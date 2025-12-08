package modulos;

import modelos.RegistroBitacora;
import modelos.UtilidadesCifrado;
import javax.crypto.SecretKey;
import java.io.Console;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModuloArchivos extends ModuloBase {

    private final String contrasena;

    public ModuloArchivos(String contrasena, Console console) {
        super(console);package modulos;

import modelos.RegistroBitacora;
import modelos.UtilidadesCifrado;
import javax.crypto.SecretKey;
import java.io.Console;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModuloArchivos extends ModuloBase {

    private final String contrasena;

    public ModuloArchivos(String contrasena, Console console) {
        super(console);
        this.contrasena = contrasena;
    }

    @Override
    public String obtenerNombre() {
        return "Cifrador de Archivos";
    }

    @Override
    public void ejecutar() throws Exception {
        boolean continuar = true;
        while (continuar) {
            System.out.println("\n--- " + obtenerNombre() + " ---");
            System.out.println("1. Cifrar archivo");
            System.out.println("2. Descifrar archivo");
            System.out.println("3. Regresar");
            System.out.print("> ");

            int opcion = leerOpcion();

            switch (opcion) {
                case 1: procesarArchivo(true); break;
                case 2: procesarArchivo(false); break;
                case 3: continuar = false; break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private void procesarArchivo(boolean esCifrado) {
        System.out.print("Ruta del archivo (usa / en lugar de \\ y sin comillas): ");
        String rutaTexto = console.readLine();
        
        if (rutaTexto == null || rutaTexto.isEmpty()) {
            System.out.println("Error: No se ingresó ninguna ruta.");
            return;
        }
        
        // Limpiar espacios al inicio y final
        rutaTexto = rutaTexto.trim();

        try {
            Path rutaArchivo = Paths.get(rutaTexto);
            
            // Si la ruta no es absoluta, convertirla a absoluta
            if (!rutaArchivo.isAbsolute()) {
                rutaArchivo = rutaArchivo.toAbsolutePath();
            }
            rutaArchivo = rutaArchivo.normalize();

            if (!Files.exists(rutaArchivo)) {
                RegistroBitacora.error("Archivo no encontrado: " + rutaArchivo.getFileName());
                System.out.println("Error: Archivo no encontrado.");
                System.out.println("Ruta buscada: " + rutaArchivo);
                return;
            }
            
            if (Files.isDirectory(rutaArchivo)) {
                System.out.println("Error: La ruta corresponde a un directorio, no a un archivo.");
                return;
            }

            // Validación simple de extensión
            boolean tieneExtensionLocked = rutaArchivo.toString().endsWith(".locked");

            if (esCifrado && tieneExtensionLocked) {
                RegistroBitacora.warn("Archivo '" + rutaArchivo.getFileName() + "' ya tiene extensión .locked.");
                System.out.println("El archivo ya parece estar cifrado.");
                return;
            }
            if (!esCifrado && !tieneExtensionLocked) {
                RegistroBitacora.warn("Extensión inválida para descifrar: " + rutaArchivo.getFileName());
                System.out.println("Para descifrar, el archivo debe terminar en .locked");
                return;
            }

            // 1. Leer archivo
            byte[] datosEntrada = Files.readAllBytes(rutaArchivo);

            // 2. Crear llave
            SecretKey clave = UtilidadesCifrado.obtenerClaveDesdeContrasena(contrasena);

            byte[] datosSalida;
            Path rutaSalida;
            
            // Usar la ruta absoluta normalizada para la salida
            String rutaAbsoluta = rutaArchivo.toString();

            if (esCifrado) {
                // Cifrar
                datosSalida = UtilidadesCifrado.cifrar(datosEntrada, clave);
                rutaSalida = Paths.get(rutaAbsoluta + ".locked");
            } else {
                // Descifrar
                datosSalida = UtilidadesCifrado.descifrar(datosEntrada, clave);
                // Quitar extensión .locked
                String nombreOriginal = rutaAbsoluta.substring(0, rutaAbsoluta.length() - ".locked".length());
                rutaSalida = Paths.get(nombreOriginal);
            }

            // 3. Guardar nuevo archivo
            try (FileOutputStream salida = new FileOutputStream(rutaSalida.toFile())) {
                salida.write(datosSalida);
            }

            // 4. Borrar original (para seguridad)
            Files.delete(rutaArchivo);

            String operacion = esCifrado ? "cifrado" : "descifrado";
            RegistroBitacora.info("Archivo '" + rutaArchivo.getFileName() + "' " + operacion + " correctamente.");
            System.out.println("Éxito. Archivo original eliminado.");
            System.out.println("Nuevo archivo: " + rutaSalida);

        } catch (Exception e) {
            String operacion = esCifrado ? "cifrar" : "descifrar";
            RegistroBitacora.error("Error al " + operacion + " archivo: " + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }
}

        this.contrasena = contrasena;
    }

    @Override
    public String obtenerNombre() {
        return "Cifrador de Archivos";
    }

    @Override
    public void ejecutar() throws Exception {
        boolean continuar = true;
        while (continuar) {
            System.out.println("\n--- " + obtenerNombre() + " ---");
            System.out.println("1. Cifrar archivo");
            System.out.println("2. Descifrar archivo");
            System.out.println("3. Regresar");
            System.out.print("> ");

            int opcion = leerOpcion();

            switch (opcion) {
                case 1: procesarArchivo(true); break;
                case 2: procesarArchivo(false); break;
                case 3: continuar = false; break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    private void procesarArchivo(boolean esCifrado) {
        System.out.print("Ruta del archivo (usa / en lugar de \\ y sin comillas): ");
        // Leer directamente del console sin usar leerLinea() que hace trim()
        String rutaTexto = console.readLine();

        if (rutaTexto == null || rutaTexto.isEmpty()) {
            System.out.println("Error: No se ingresó ninguna ruta.");
            return;
        }

        // Limpiar espacios al inicio y final
        rutaTexto = rutaTexto.trim();

        // Limpiar comillas al inicio y final si el usuario las incluyó
        if ((rutaTexto.startsWith("\"") && rutaTexto.endsWith("\"")) ||
                (rutaTexto.startsWith("'") && rutaTexto.endsWith("'"))) {
            rutaTexto = rutaTexto.substring(1, rutaTexto.length() - 1);
        }

        try {
            Path rutaArchivo = Paths.get(rutaTexto);

            // Si la ruta no es absoluta, convertirla a absoluta
            if (!rutaArchivo.isAbsolute()) {
                rutaArchivo = rutaArchivo.toAbsolutePath();
            }
            rutaArchivo = rutaArchivo.normalize();

            if (!Files.exists(rutaArchivo)) {
                RegistroBitacora.error("Archivo no encontrado: " + rutaArchivo.getFileName());
                System.out.println("Error: Archivo no encontrado.");
                System.out.println("Ruta buscada: " + rutaArchivo);
                return;
            }

            if (Files.isDirectory(rutaArchivo)) {
                System.out.println("Error: La ruta corresponde a un directorio, no a un archivo.");
                return;
            }

            // Validación simple de extensión
            boolean tieneExtensionLocked = rutaArchivo.toString().endsWith(".locked");

            if (esCifrado && tieneExtensionLocked) {
                RegistroBitacora.warn("Archivo '" + rutaArchivo.getFileName() + " ya tiene extension .locked");
                System.out.println("El archivo ya parece estar cifrado.");
                return;
            }
            if (!esCifrado && !tieneExtensionLocked) {
                RegistroBitacora.warn("Extension invalida para descifrar " + rutaArchivo.getFileName());
                System.out.println("Para descifrar, el archivo debe terminar en .locked");
                return;
            }

            // 1. Leer archivo
            byte[] datosEntrada = Files.readAllBytes(rutaArchivo);

            // 2. Crear llave
            SecretKey clave = UtilidadesCifrado.obtenerClaveDesdeContrasena(contrasena);

            byte[] datosSalida;
            Path rutaSalida;

            // Usar la ruta absoluta normalizada para la salida
            String rutaAbsoluta = rutaArchivo.toString();

            if (esCifrado) {
                // Cifrar
                datosSalida = UtilidadesCifrado.cifrar(datosEntrada, clave);
                rutaSalida = Paths.get(rutaAbsoluta + ".locked");
            } else {
                // Descifrar
                datosSalida = UtilidadesCifrado.descifrar(datosEntrada, clave);
                // Quitar extensión .locked
                String nombreOriginal = rutaAbsoluta.substring(0, rutaAbsoluta.length() - ".locked".length());
                rutaSalida = Paths.get(nombreOriginal);
            }

            // 3. Guardar nuevo archivo
            try (FileOutputStream salida = new FileOutputStream(rutaSalida.toFile())) {
                salida.write(datosSalida);
            }

            // 4. Borrar original (para seguridad)
            Files.delete(rutaArchivo);

            String operacion = esCifrado ? "cifrado" : "descifrado";
            RegistroBitacora.info("Archivo '" + rutaArchivo.getFileName() + "' " + operacion + " correctamente.");
            System.out.println("Éxito. Archivo original eliminado.");
            System.out.println("Nuevo archivo: " + rutaSalida);

        } catch (Exception e) {
            String operacion = esCifrado ? "cifrar" : "descifrar";
            RegistroBitacora.error("Error al " + operacion + " archivo: " + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }
}
