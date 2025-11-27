package modulos;

import modelos.Boveda;
import modelos.AlmacenamientoBoveda;
import modelos.UtilidadesCifrado;
import java.io.Console;

public class ModuloBoveda extends ModuloBase {

    private static final int MAX_INTENTOS_CONTRASENA = 3;

    private final Boveda boveda;
    private final AlmacenamientoBoveda almacenamiento;
    private String contrasena; // No es final para permitir cambio de contraseña

    public ModuloBoveda(Boveda boveda, AlmacenamientoBoveda almacenamiento, String contrasena, Console console) {
        super(console);
        this.boveda = boveda;
        this.almacenamiento = almacenamiento;
        this.contrasena = contrasena;
    }

    @Override
    public String obtenerNombre() {
        return "Gestor de Contraseñas";
    }

    /**
     * Obtiene la contraseña actual (puede haber cambiado durante la sesión).
     */
    public String getContrasena() {
        return contrasena;
    }

    @Override
    public void ejecutar() throws Exception {
        boolean continuar = true;
        while (continuar) {
            System.out.println("\n--- " + obtenerNombre() + " ---");

            boolean estaVacia = boveda.listarNombresSecretos().isEmpty();

            // Menú dinámico
            System.out.println("1. Agregar secreto");
            if (estaVacia) {
                System.out.println("2. Cambiar contraseña maestra");
                System.out.println("3. Guardar y Regresar");
            } else {
                System.out.println("2. Ver secreto");
                System.out.println("3. Listar secretos");
                System.out.println("4. Eliminar secreto");
                System.out.println("5. Cambiar contraseña maestra");
                System.out.println("6. Guardar y Regresar");
            }
            System.out.print("> ");

            int opcion = leerOpcion();

            // Ajuste de lógica si está vacía (para que el menú coincida)
            if (estaVacia) {
                // Menú vacío: 1=Agregar, 2=Cambiar contraseña, 3=Guardar
                if (opcion == 2) opcion = 5; // Cambiar contraseña
                else if (opcion == 3) opcion = 6; // Guardar y regresar
                else if (opcion != 1) opcion = -1;
            }

            switch (opcion) {
                case 1:
                    System.out.print("Nombre del secreto: ");
                    String nombre = leerLinea();
                    System.out.print("Valor del secreto: ");
                    String valor = leerLinea();
                    boveda.agregarSecreto(nombre, valor);
                    break;
                case 2:
                    System.out.print("Nombre a buscar: ");
                    String buscar = leerLinea();
                    System.out.println("Valor: " + boveda.obtenerSecreto(buscar));
                    break;
                case 3:
                    System.out.println("--- Lista de Secretos ---");
                    boveda.listarNombresSecretos().forEach(System.out::println);
                    break;
                case 4:
                    System.out.print("Nombre a eliminar: ");
                    String eliminar = leerLinea();
                    boveda.eliminarSecreto(eliminar);
                    break;
                case 5:
                    cambiarContrasenaMaestra();
                    break;
                case 6:
                    System.out.println("Guardando...");
                    almacenamiento.guardarBoveda(boveda, contrasena);
                    System.out.println("¡Guardado! Regresando...");
                    continuar = false;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    /**
     * Permite cambiar la contraseña maestra de la bóveda.
     * Valida la contraseña actual (máximo 3 intentos), aplica política de seguridad
     * a la nueva contraseña, y re-cifra la bóveda.
     *
     * @return true si el cambio fue exitoso, false si se canceló o falló
     */
    public boolean cambiarContrasenaMaestra() {
        System.out.println("\n--- Cambiar Contraseña Maestra ---");

        // Paso 1: Verificar contraseña actual (máximo 3 intentos)
        int intentos = 0;
        boolean contrasenaVerificada = false;

        while (intentos < MAX_INTENTOS_CONTRASENA && !contrasenaVerificada) {
            String contrasenaIngresada = leerContrasenaOculta("Ingrese su contraseña actual: ");

            try {
                // Intentamos cargar la bóveda para verificar la contraseña
                almacenamiento.cargarBoveda(contrasenaIngresada);
                contrasenaVerificada = true;
            } catch (Exception e) {
                intentos++;
                int restantes = MAX_INTENTOS_CONTRASENA - intentos;
                if (restantes > 0) {
                    System.out.println("⚠ Contraseña incorrecta. Intentos restantes: " + restantes);
                } else {
                    System.out.println("\n✖ Se agotaron los intentos. Operación cancelada por seguridad.");
                    return false;
                }
            }
        }

        // Paso 2: Mostrar requisitos y advertencia
        System.out.println();
        System.out.println(UtilidadesCifrado.REQUISITOS_CONTRASENA);
        System.out.println();
        System.out.println("⚠ ADVERTENCIA: Los archivos .locked cifrados con el Cifrador de Archivos");
        System.out.println("  seguirán usando la contraseña anterior. Descífrelos antes de cambiar");
        System.out.println("  la contraseña o recuerde la contraseña antigua.");
        System.out.println();

        // Paso 3: Solicitar y validar nueva contraseña
        String nuevaContrasena;
        String errorValidacion = null;

        do {
            nuevaContrasena = leerContrasenaOculta("Nueva contraseña: ");

            // Verificar que no sea igual a la actual
            if (nuevaContrasena.equals(contrasena)) {
                System.out.println("\n⚠ La nueva contraseña no puede ser igual a la actual.");
                System.out.println("Intenta de nuevo.\n");
                errorValidacion = "igual"; // Marcador para continuar el bucle
                continue;
            }

            errorValidacion = UtilidadesCifrado.validarPoliticaContrasena(nuevaContrasena);

            if (errorValidacion != null) {
                System.out.println("\n⚠ " + errorValidacion);
                System.out.println("Intenta de nuevo.\n");
            }
        } while (errorValidacion != null);

        // Paso 4: Confirmar nueva contraseña
        String confirmacion = leerContrasenaOculta("Confirme la nueva contraseña: ");

        if (!nuevaContrasena.equals(confirmacion)) {
            System.out.println("\n✖ Las contraseñas no coinciden. Operación cancelada.");
            return false;
        }

        // Paso 5: Re-cifrar la bóveda con la nueva contraseña
        try {
            almacenamiento.guardarBoveda(boveda, nuevaContrasena);
            this.contrasena = nuevaContrasena;
            System.out.println("\n✔ ¡Contraseña maestra cambiada exitosamente!");
            return true;
        } catch (Exception e) {
            System.out.println("\n✖ Error al guardar la bóveda: " + e.getMessage());
            System.out.println("La contraseña NO fue cambiada.");
            return false;
        }
    }

    /**
     * Lee una contraseña ocultando la entrada del usuario.
     */
    private String leerContrasenaOculta(String mensaje) {
        char[] passwordArray = console.readPassword(mensaje);
        if (passwordArray == null || passwordArray.length == 0) {
            return "";
        }
        String resultado = new String(passwordArray);
        // Limpiamos el array por seguridad
        java.util.Arrays.fill(passwordArray, '\0');
        return resultado;
    }
}