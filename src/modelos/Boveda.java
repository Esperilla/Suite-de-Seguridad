package modelos;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**    
     * Verifica si un secreto existe en la bóveda.
     *
     * @param nombre Nombre del secreto a verificar
     * @return true si el secreto existe, false en caso contrario
     */
    public boolean existeSecreto(String nombre) {
        return secretos.containsKey(nombre);
    }

    /**
     * Actualiza el valor de un secreto existente.
     *
     * @param nombre Nombre del secreto a actualizar
     * @param nuevoValor Nuevo valor para el secreto
     * @return true si se actualizó correctamente, false si el secreto no existe
     */
    public boolean actualizarSecreto(String nombre, String nuevoValor) {
        if (!secretos.containsKey(nombre)) {
            System.out.println("Error: El secreto '" + nombre + "' no existe.");
            return false;
        }
        secretos.put(nombre, nuevoValor);
        System.out.println("✔ Secreto '" + nombre + "' actualizado correctamente.");
        return true;
    } 

    // --- Métodos de Búsqueda Inteligente ---

    /**
     * Calcula la distancia de Levenshtein entre dos cadenas.
     * La distancia representa el número mínimo de operaciones (inserción, eliminación, sustitución)
     * necesarias para transformar una cadena en otra.
     *
     * @param a Primera cadena
     * @param b Segunda cadena
     * @return Distancia de edición entre las dos cadenas
     */
    private int calcularDistanciaLevenshtein(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();

        int[][] dp = new int[a.length() + 1][b.length() + 1];

        // Inicializar primera columna y fila
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        // Llenar la matriz
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int costo = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(
                                dp[i - 1][j] + 1,      // Eliminación
                                dp[i][j - 1] + 1       // Inserción
                        ),
                        dp[i - 1][j - 1] + costo   // Sustitución
                );
            }
        }

        return dp[a.length()][b.length()];
    }

    /**
     * Calcula una puntuación de similitud para ordenar resultados.
     * Menor puntuación = mayor relevancia.
     * Prioriza: coincidencia exacta > contiene > Levenshtein cercano
     *
     * @param nombreSecreto Nombre del secreto almacenado
     * @param patron Patrón de búsqueda del usuario
     * @return Puntuación de similitud (menor es mejor)
     */
    private int calcularPuntuacionSimilitud(String nombreSecreto, String patron) {
        String nombreLower = nombreSecreto.toLowerCase();
        String patronLower = patron.toLowerCase();

        // Coincidencia exacta (máxima prioridad)
        if (nombreLower.equals(patronLower)) {
            return 0;
        }

        // Comienza con el patrón
        if (nombreLower.startsWith(patronLower)) {
            return 1;
        }

        // Contiene el patrón
        if (nombreLower.contains(patronLower)) {
            return 2;
        }

        // Distancia Levenshtein (ajustada para ordenamiento)
        int distancia = calcularDistanciaLevenshtein(nombreSecreto, patron);
        return 10 + distancia; // Base de 10 para que Levenshtein tenga menor prioridad que contains
    }

    /**
     * Busca secretos de forma inteligente usando múltiples estrategias:
     * 1. Coincidencia exacta (ignorando mayúsculas/minúsculas)
     * 2. Coincidencia parcial (contiene el patrón)
     * 3. Similitud por distancia Levenshtein (tolerancia a errores tipográficos)
     *
     * @param patron Patrón de búsqueda ingresado por el usuario
     * @param maxResultados Número máximo de resultados a retornar
     * @return Lista de nombres de secretos ordenados por relevancia
     */
    public List<String> buscarSecretosInteligente(String patron, int maxResultados) {
        if (patron == null || patron.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String patronLower = patron.toLowerCase().trim();

        // Umbral dinámico basado en la longitud del patrón:
        // - Patrón de 1-2 caracteres: solo coincidencia exacta o contains (umbral 0)
        // - Patrón de 3-4 caracteres: permitir 1 error tipográfico
        // - Patrón de 5-7 caracteres: permitir 2 errores
        // - Patrón de 8+ caracteres: permitir 3 errores
        int umbralLevenshtein;
        if (patronLower.length() <= 2) {
            umbralLevenshtein = 0; // Solo exacto o contains para búsquedas muy cortas
        } else if (patronLower.length() <= 4) {
            umbralLevenshtein = 1;
        } else if (patronLower.length() <= 7) {
            umbralLevenshtein = 2;
        } else {
            umbralLevenshtein = 3;
        }

        final int umbralFinal = umbralLevenshtein;

        // Filtrar secretos que coincidan por algún criterio
        List<String> resultados = secretos.keySet().stream()
                .filter(nombre -> nombre != null && !nombre.trim().isEmpty()) // Ignorar nombres vacíos
                .filter(nombre -> {
                    String nombreLower = nombre.toLowerCase().trim();
                    // Incluir si: coincide exacto, contiene el patrón, o distancia Levenshtein <= umbral
                    boolean coincideExacto = nombreLower.equals(patronLower);
                    boolean contienePatron = nombreLower.contains(patronLower);
                    boolean cercanoLevenshtein = umbralFinal > 0 &&
                            calcularDistanciaLevenshtein(nombreLower, patronLower) <= umbralFinal;

                    return coincideExacto || contienePatron || cercanoLevenshtein;
                })
                .sorted(Comparator.comparingInt(nombre -> calcularPuntuacionSimilitud(nombre, patron)))
                .limit(maxResultados)
                .collect(Collectors.toList());

        return resultados;
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
