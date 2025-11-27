package modelos;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class UtilidadesCifrado {

    // Constantes para configurar el cifrado
    private static final String ALGORITMO_HASH = "SHA-256";
    private static final String ALGORITMO_CIFRADO = "AES/GCM/NoPadding"; // Modo GCM más seguro
    private static final int GCM_IV_LENGTH = 12; // Longitud recomendada para IV en GCM
    private static final int GCM_TAG_LENGTH = 128; // Bits para el tag de autenticación

    // Constantes para política de contraseñas
    private static final int LONGITUD_MINIMA_CONTRASENA = 8;
    private static final String CARACTERES_ESPECIALES = "!@#$%^&*()-_=+[]{}|;:',.<>?/~`";

    /**
     * Mensaje con los requisitos de contraseña para mostrar al usuario.
     */
    public static final String REQUISITOS_CONTRASENA =
            "Requisitos de la contraseña:\n" +
                    "  • Mínimo 8 caracteres\n" +
                    "  • Al menos una letra mayúscula\n" +
                    "  • Al menos un número\n" +
                    "  • Al menos un carácter especial (!@#$%^&* etc.)";

    /**
     * Valida que una contraseña cumpla con la política de seguridad.
     * @param contrasena La contraseña a validar
     * @return null si la contraseña es válida, o un String con el error específico
     */
    public static String validarPoliticaContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            return "La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres.";
        }

        boolean tieneMayuscula = false;
        boolean tieneNumero = false;
        boolean tieneEspecial = false;

        for (char c : contrasena.toCharArray()) {
            if (Character.isUpperCase(c)) {
                tieneMayuscula = true;
            } else if (Character.isDigit(c)) {
                tieneNumero = true;
            } else if (CARACTERES_ESPECIALES.indexOf(c) != -1) {
                tieneEspecial = true;
            }
        }

        if (!tieneMayuscula) {
            return "La contraseña debe contener al menos una letra mayúscula.";
        }
        if (!tieneNumero) {
            return "La contraseña debe contener al menos un número.";
        }
        if (!tieneEspecial) {
            return "La contraseña debe contener al menos un carácter especial (!@#$%^&* etc.).";
        }

        return null; // Contraseña válida
    }

    /**
     * Convierte texto (contraseña) en una Llave secreta válida para AES.
     */
    public static SecretKey obtenerClaveDesdeContrasena(String contrasena) throws Exception {
        if (contrasena == null || contrasena.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        // Usamos SHA-256 para "triturar" la contraseña y obtener bytes fijos
        MessageDigest digest = MessageDigest.getInstance(ALGORITMO_HASH);
        byte[] bytesClave = digest.digest(contrasena.getBytes(StandardCharsets.UTF_8));

        // Retornamos la llave lista
        return new SecretKeySpec(bytesClave, "AES");
    }

    /**
     * Encripta (Cifra) datos usando AES-GCM con IV aleatorio.
     * El IV se prepende a los datos cifrados.
     */
    public static byte[] cifrar(byte[] datos, SecretKey clave) throws Exception {
        // Generamos un IV aleatorio para cada cifrado
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cifrador = Cipher.getInstance(ALGORITMO_CIFRADO);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cifrador.init(Cipher.ENCRYPT_MODE, clave, gcmSpec);

        byte[] datosCifrados = cifrador.doFinal(datos);

        // Combinamos IV + datos cifrados para almacenar juntos
        ByteBuffer buffer = ByteBuffer.allocate(iv.length + datosCifrados.length);
        buffer.put(iv);
        buffer.put(datosCifrados);

        return buffer.array();
    }

    /**
     * Desencripta (Descifra) datos. Extrae el IV del inicio de los datos.
     */
    public static byte[] descifrar(byte[] datosCifrados, SecretKey clave) throws Exception {
        // Extraemos el IV del inicio
        ByteBuffer buffer = ByteBuffer.wrap(datosCifrados);
        byte[] iv = new byte[GCM_IV_LENGTH];
        buffer.get(iv);

        // El resto son los datos cifrados reales
        byte[] datosReales = new byte[buffer.remaining()];
        buffer.get(datosReales);

        Cipher cifrador = Cipher.getInstance(ALGORITMO_CIFRADO);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cifrador.init(Cipher.DECRYPT_MODE, clave, gcmSpec);

        return cifrador.doFinal(datosReales);
    }
}