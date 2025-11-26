package modelos;

import java.io.*;
import javax.crypto.SecretKey;

public class AlmacenamientoBoveda {

    private static final String NOMBRE_ARCHIVO = "boveda.dat";


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
            return Boveda.crearDesdeBytes(datosDescifrados);

        } catch (javax.crypto.BadPaddingException e) {
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
    }
}