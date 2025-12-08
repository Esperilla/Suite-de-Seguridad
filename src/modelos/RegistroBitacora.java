package modelos;


import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de registro de eventos (Bitacora) para auditoria.
 * Registra acciones con fecha y hora en el archivo logs/audit.log
 *
 * Formato de línea: [YYYY-MM-DD HH:mm:ss] Nivel: mensaje
 * Ejemplo: [2025-11-20 10:00:00] INFO: se agregó secreto 'Gmail'.
 *
 * Importante: solo se registran nombres de secretos, NUNCA valores sensibles.
 */
public class RegistroBitacora {
    private static final String CARPETA_LOGS = "logs";
    private static final String ARCHIVO_LOG = "audit.log";
    private static final int DIAS_RETENCION = 7;

    // Formateadores de fecha/hora
    private static final DateTimeFormatter FORMATO_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    //Flag para ejecutar limpieza solo una vez por sesión
    private static boolean limpiezaEjecutada = false;

    // Ruta calculada del archivo de log
    private static final String RUTA_ARCHIVO_LOG;

    static {
        String rutaCalculada;
        // Obtener la ubicación base del classpath
        try {
            File ubicacionClase = new File(
                    RegistroBitacora.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            // subir 1 nivel para llegar a la raíz del proyecto
            File raizProyecto = ubicacionClase.getParentFile();

            //Crear carpeta logs si no existe
            File carpetaLogs = new File(raizProyecto, CARPETA_LOGS);
            if(!carpetaLogs.exists()){
                carpetaLogs.mkdirs();
            }

            rutaCalculada = new File(carpetaLogs,CARPETA_LOGS).getAbsolutePath();
        }catch (URISyntaxException | NullPointerException e){
            // Fallback a ruta relativa si falla la detención
            File carpetaLogs = new File(CARPETA_LOGS);
            if (!carpetaLogs.exists()){
                carpetaLogs.mkdirs();
            }
            rutaCalculada = new File(carpetaLogs,ARCHIVO_LOG).getAbsolutePath();
        }
        RUTA_ARCHIVO_LOG = rutaCalculada;
    }

    /**
     * Registra un evento a nivel INFO
     * Usar para operaciones exitosas y normales.
     *
     * @param mensaje Descripcion del evento (No incluir valores sensibles)
     **/
    public static void info(String mensaje){
        registrar( "INFO", mensaje);
    }

    /**
     * Registra un evento de nivel WARN.
     * Usar para advertencias y situaciones que requieren atención.
     *
     * @param mensaje Descripción del evento (no incluir valores sensibles)
     */
    public static void warn(String mensaje) {
        registrar("WARN", mensaje);
    }

    /**
     * Registra un evento de nivel ERROR.
     * Usar para errores y fallos del sistema.
     *
     * @param mensaje Descripción del evento (no incluir valores sensibles)
     */
    public static void error(String mensaje) {
        registrar("ERROR", mensaje);
    }

    /**
     * Método principal de registro.
     * Formatea la línea y la escribe al archivo.
     * Ejecuta limpieza de logs antiguos una vez por sesión.
     *
     * @param nivel Nivel del evento (INFO, WARN, ERROR)
     * @param mensaje Descripción del evento
     */

    private static synchronized void registrar(String nivel, String mensaje){
        //Ejecutar limpieza una vez por sesión
        if(!limpiezaEjecutada){
            limpiarLogsAntiguos();
            limpiezaEjecutada = true;
        }
        // Formatear línea: [2025-11-28 10:00:00] INFO: mensaje
        String timestamp = LocalDateTime.now().format(FORMATO_TIMESTAMP);
        String linea = String.format("[%s] %s: %s", timestamp, nivel, mensaje);

        // Escribir al archivo (append mode)
        try (FileWriter fw = new FileWriter(RUTA_ARCHIVO_LOG, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {

            pw.println(linea);
            pw.flush(); // Flush inmediato para evitar pérdida de datos

        } catch (IOException e) {
            // Silencioso: el logging no debe interrumpir la aplicación
            System.err.println("[RegistroBitacora] Error al escribir log: " + e.getMessage());
        }
    }

    /**
     * Elimina líneas de log con más de 7 días de antigüedad.
     * Lee el archivo, filtra líneas antiguas y reescribe el archivo.
     */

    private static void limpiarLogsAntiguos(){
        File archivoLog = new File(RUTA_ARCHIVO_LOG);

        // Si el archivo no existe, no hay nada que limpiar
        if (!archivoLog.exists()) {
            return;
        }

        try{
            // Leer todas las líneas
            List<String> lineas = Files.readAllLines(archivoLog.toPath());
            List<String> lineasValidas = new ArrayList<>();

            LocalDate fechaLimite = LocalDate.now().minusDays(DIAS_RETENCION);

            for (String linea : lineas) {
                // Extraer fecha de la línea [YYYY-MM-DD HH:mm:ss]
                LocalDate fechaLinea = extraerFechaDeLinea(linea);

                // Mantener línea si no pudimos extraer fecha o si está dentro del período
                if (fechaLinea == null || !fechaLinea.isBefore(fechaLimite)) {
                    lineasValidas.add(linea);
                }
            }

            // Solo reescribir si hubo cambios
            if (lineasValidas.size() < lineas.size()) {
                Files.write(archivoLog.toPath(), lineasValidas);
            }

        } catch (IOException e) {
            // Silencioso: el logging no debe interrumpir la aplicación
            System.err.println("[RegistroBitacora] Error al limpiar logs antiguos: " + e.getMessage());
        }
    }

    /**
     * Extrae la fecha de una línea de log.
     * Formato esperado: [YYYY-MM-DD HH:mm:ss] ...
     *
     * @param linea Línea del archivo de log
     * @return LocalDate extraído o null si no se puede parsear
     */

    private static LocalDate extraerFechaDeLinea(String linea){
        if (linea == null || linea.length() < 12){
            return null;
        }
        try {
            // Extraer solo la parte de fecha [YYYY-MM-DD
            if (linea.startsWith("[") && linea.length() >= 11) {
                String fechaStr = linea.substring(1, 11); // "YYYY-MM-DD"
                return LocalDate.parse(fechaStr, FORMATO_FECHA);
            }
        }catch (DateTimeParseException e){
            // Línea con formato inválido, retornar null
        }
        return null;
    }



}
