package modulos;

import java.io.Console;

public abstract class ModuloBase {

    // Usamos Console en lugar de Scanner para evitar problemas de buffer
    protected Console console;

    // Constructor: Recibe el Console del Main y lo guarda
    public ModuloBase(Console console) {
        this.console = console;
    }

    // Metodo utilitario para leer números
    protected int leerOpcion() {
        try {
            String linea = console.readLine().trim();
            return Integer.parseInt(linea);
        } catch (NumberFormatException e) {
            return -1; // Opción inválida
        }
    }

    // Metodo utilitario para leer texto
    protected String leerLinea() {
        return console.readLine().trim();
    }

    // --- METODOS ABSTRACTOS (Polimorfismo) ---

    public abstract void ejecutar() throws Exception;

    public abstract String obtenerNombre();
}
