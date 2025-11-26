package modelos;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Representa la bóveda en memoria.
 * Guarda SOLAMENTE texto (String, String).
 * String: nombre del secreto, String: valor del secreto.
 */
public class Boveda implements Serializable {

    @Serial
    private static final long serialVersionUID = 20L;

    // Mapa simple de String a String
    // Map se usa para almacenar pares clave-valor
    private Map<String, String> secretos;

    public Boveda() {
        this.secretos = new HashMap<>();
    }

    // --- Métodos de gestión ---

    public void agregarSecreto(String nombre, String value) {
        secretos.put(nombre, value);
        System.out.println("Secreto '" + nombre + "' agregado.");
    }

    public String obtenerSecreto(String nombre) {
        return secretos.getOrDefault(nombre, "Error: Secreto no encontrado.");
    }

    public Set<String> listarNombresSecretos() {
        return secretos.keySet();
    }

    public void eliminarSecreto(String nombre) {
        if (secretos.remove(nombre) != null) {
            System.out.println("🗑️ Secreto '" + nombre + "' eliminado.");
        } else {
            System.out.println("Error: No se encontró el secreto '" + nombre + "'.");
        }
    }

    // --- Métodos de Serialización (Conversión a bytes) ---

    public byte[] convertirABytes() throws IOException {
        // Usamos try-with-resources para cerrar los streams automáticamente
        try (ByteArrayOutputStream flujoBytes = new ByteArrayOutputStream();
             ObjectOutputStream flujoObjetos = new ObjectOutputStream(flujoBytes)) {

            flujoObjetos.writeObject(this.secretos); // Guardamos el mapa
            flujoObjetos.flush(); // Aseguramos que todos los datos se escriban
            return flujoBytes.toByteArray();
        }
    }

    // Archivo: `src/modelos/Vault.java`
    public static Boveda crearDesdeBytes(byte[] data) throws Exception {

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
             ObjectInputStream objIn = new ObjectInputStream(byteIn)) {

            Object obj = objIn.readObject();

            if (obj instanceof Map<?, ?> raw) {
                Map<String, String> safe = new HashMap<>(raw.size());

                for (Map.Entry<?, ?> entry : raw.entrySet()) {
                    Object k = entry.getKey();
                    Object v = entry.getValue();
                    if (!(k instanceof String) || !(v instanceof String)) {
                        throw new IOException("El Map contiene claves/valores que no son String.");
                    }
                    safe.put((String) k, (String) v);
                }

                Boveda nuevaBoveda = new Boveda();
                nuevaBoveda.secretos = safe;
                return nuevaBoveda;
            } else {
                throw new IOException("Los datos no contienen un Map válido.");
            }
        }
    }
}