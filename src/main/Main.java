package main;

import modelos.Boveda;
import modelos.AlmacenamientoBoveda;
import modelos.RegistroBitacora;
import modelos.UtilidadesCifrado;
import modulos.ModuloBase;
import modulos.ModuloArchivos;
import modulos.ModuloBoveda;
import java.io.Console;

public class Main {

    // Console para TODA la entrada del usuario (evita problemas de buffer)
    private static final Console console = System.console();
    // Almacenamiento de la bóveda para guardar/cargar la bóveda
    private static final AlmacenamientoBoveda almacenamiento = new AlmacenamientoBoveda();
    // Bóveda actual en memoria
    private static Boveda boveda;
    // Contraseña maestra actual
    private static String contrasenaActual;


    // --- Metodo Principal ---
    public static void main(String[] args) {
        // Verificamos que Console esté disponible (solo funciona en terminal real)
        if (console == null) {
            RegistroBitacora.error("Programa ejecutado sin consola real.");
            System.err.println("Error: Este programa debe ejecutarse desde una terminal/consola real.");
            System.err.println("No se puede ejecutar desde un IDE. Use: java -cp <classpath> main.Main");
            System.exit(1);
        }

        System.out.println("--- SUITE DE SEGURIDAD (JAVA POO) ---");

        try {
            // Login (Existe bóveda o no)
            if (almacenamiento.existeBoveda()) {
                iniciarSesion();
            } else {
                crearNuevaBoveda();
            }

            // POLIMORFISMO: Usamos la clase Padre (ModuloBase) para referirnos a los hijos
            // Nota: moduloBoveda es tipo concreto para acceder a getContrasena()
            ModuloBoveda moduloBoveda = new ModuloBoveda(boveda, almacenamiento, contrasenaActual, console);
            ModuloBase moduloArchivos = new ModuloArchivos(contrasenaActual, console);

            // Menú Principal
            boolean enEjecucion = true;

            while (enEjecucion) {
                System.out.println("\n--- Menú Principal ---");
                System.out.println("1. " + moduloBoveda.obtenerNombre());
                System.out.println("2. " + moduloArchivos.obtenerNombre());
                System.out.println("3. Salir");

                String opcion = console.readLine("> ").trim();

                switch (opcion) {
                    case "1":
                        moduloBoveda.ejecutar();
                        // Verificar si la contraseña cambió durante la ejecución
                        if (!contrasenaActual.equals(moduloBoveda.getContrasena())) {
                            contrasenaActual = moduloBoveda.getContrasena();
                            // Recrear ModuloArchivos con la nueva contraseña
                            moduloArchivos = new ModuloArchivos(contrasenaActual, console);
                            System.out.println("(El Cifrador de Archivos ahora usa la nueva contraseña)");
                        }
                        break;
                    case "2":
                        moduloArchivos.ejecutar();
                        break;
                    case "3":
                        RegistroBitacora.info("Aplicación cerrada por el usuario");
                        System.out.println("Cerrando programa... ¡Adiós!");
                        enEjecucion = false;
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }

        } catch (Exception e) {
            RegistroBitacora.error("Error Fatal: " + e.getMessage());
            System.err.println("Error fatal: " + e.getMessage());
        }
    }

    // --- Metodos de Login ---

    private static void crearNuevaBoveda() throws Exception {
        System.out.println("\n--- Configuración Inicial ---");
        System.out.println("Bienvenido. Crea una contraseña maestra.");
        System.out.println();
        System.out.println(UtilidadesCifrado.REQUISITOS_CONTRASENA);
        System.out.println();

        String nuevaContra;
        String errorValidacion;

        // Bucle hasta que la contraseña cumpla la política
        do {
            nuevaContra = leerContrasena("Nueva contraseña: ");
            errorValidacion = UtilidadesCifrado.validarPoliticaContrasena(nuevaContra);

            if (errorValidacion != null) {
                System.out.println("\n⚠ " + errorValidacion);
                System.out.println("Intenta de nuevo.\n");
            }
        } while (errorValidacion != null);

        String nuevaContraVerificada = leerContrasena("Confirma contraseña: ");

        if (!nuevaContra.equals(nuevaContraVerificada)) {
            RegistroBitacora.warn("Creación de bóveda cancelada: las contraseñas no coinciden.");
            throw new Exception("Las contraseñas no coinciden.");
        }

        contrasenaActual = nuevaContra;
        boveda = new Boveda();
        almacenamiento.guardarBoveda(boveda, contrasenaActual);
        RegistroBitacora.info("Nueva bóveda creada exitosamente. ");
        System.out.println("¡Sistema configurado correctamente!");
    }

    private static void iniciarSesion() throws Exception {
        System.out.println("\n--- Inicio de Sesión ---");
        String contraAlmacenada = leerContrasena("Introduce tu contraseña maestra: ");

        if (contraAlmacenada == null || contraAlmacenada.trim().isEmpty()) {
            throw new Exception("La contraseña no puede estar vacía.");
        }

        boveda = almacenamiento.cargarBoveda(contraAlmacenada);
        contrasenaActual = contraAlmacenada;
        RegistroBitacora.info("Inicio de sesión exitoso");
        System.out.println("¡Acceso concedido!");
    }

    // Lee contraseña ocultando la entrada
    private static String leerContrasena(String mensaje) {
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
