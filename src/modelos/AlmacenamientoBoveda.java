package modelos;

import java.io.*;
import java.net.URISyntaxException;
import javax.crypto.SecretKey;

import static modelos.RegistroBitacora.*;

public class AlmacenamientoBoveda {

    private static final String NOMBRE_ARCHIVO;

    static {
        String rutaCalculada;
        try {
            // Obtener la ubicación base del classpath (carpeta out/)
            File ubicacionClase = new File(
                    AlmacenamientoBoveda.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            // getCodeSource() devuelve la raíz del classpath (out/), subir 1 nivel para llegar a la raíz del proyecto
            File raizProyecto = ubicacionClase.getParentFile();

            rutaCalculada = new File(raizProyecto, "boveda.dat").getAbsolutePath();

            System.out.println("[INFO] Ruta de boveda.dat: " + rutaCalculada);

        } catch (URISyntaxException | NullPointerException e) {
            // Fallback a ruta relativa si falla la detección
            rutaCalculada = "boveda.dat";
            System.out.println("[ADVERTENCIA] No se pudo calcular la ruta absoluta, usando ruta relativa: " + rutaCalculada);
        }
        NOMBRE_ARCHIVO = rutaCalculada;
    }


    public boolean existeBoveda() {
        return new File(NOMBRE_ARCHIVO).exists();
    }

    // Cargar archivo del disco -> Descifrar -> Convertir en Objeto
    public Boveda cargarBoveda(String contrasena) throws Exception {
        // Usamos obtenerArchivo()
        try (FileInputStream archivoEntrada = new FileInputStream(NOMBRE_ARCHIVO)) {

            // 1. Leemos los bytes cifrados del disco
            byte[] datosCifrados = archivoEntrada.readAllBytes();

            // 2. Preparamos la llave
            SecretKey clave = UtilidadesCifrado.obtenerClaveDesdeContrasena(contrasena);

            // 3. Desciframos
            byte[] datosDescifrados = UtilidadesCifrado.descifrar(datosCifrados, clave);

            // 4. Convertimos bytes a Objeto Boveda

            Boveda bovedaCargada = Boveda.crearDesdeBytes(datosDescifrados);
            info("Bóveda cargada correctamente.");
            return bovedaCargada;

        } catch (javax.crypto.BadPaddingException e) {
            warn("Intento de descifrado fallido");
            throw new Exception("Contraseña incorrecta o archivo dañado.");
        }
    }

    // Objeto Boveda -> Convertir a Bytes -> Cifrar -> Guardar en disco
    public void guardarBoveda(Boveda boveda, String contrasena) throws Exception {

        SecretKey clave = UtilidadesCifrado.obtenerClaveDesdeContrasena(contrasena);

        byte[] datosOriginales = boveda.convertirABytes();
        byte[] datosCifrados = UtilidadesCifrado.cifrar(datosOriginales, clave);

        // Usamos obtenerArchivo()
        try (FileOutputStream archivoSalida = new FileOutputStream(NOMBRE_ARCHIVO)) {
            archivoSalida.write(datosCifrados);
        }
        info("Bóveda guardada correctamente.");
    }
}