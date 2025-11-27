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
}
