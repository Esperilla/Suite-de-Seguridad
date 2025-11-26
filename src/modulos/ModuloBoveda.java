package modulos;

import modelos.Boveda;
import modelos.AlmacenamientoBoveda;
import java.io.Console;

public class ModuloBoveda extends ModuloBase {

    private final Boveda boveda;
    private final AlmacenamientoBoveda almacenamiento;
    private final String contrasena;

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

    @Override
    public void ejecutar() throws Exception {
        boolean continuar = true;
        while (continuar) {
            System.out.println("\n--- " + obtenerNombre() + " ---");

            boolean estaVacia = boveda.listarNombresSecretos().isEmpty();

            // Menú dinámico
            System.out.println("1. Agregar secreto");
            if (estaVacia) {
                System.out.println("4. Guardar y Regresar");
            } else {
                System.out.println("2. Ver secreto");
                System.out.println("3. Listar secretos");
                System.out.println("4. Eliminar secreto");
                System.out.println("5. Guardar y Regresar");
            }
            System.out.print("> ");

            int opcion = leerOpcion();

            // Ajuste de lógica si está vacía (para que el menú coincida)
            if (estaVacia) {
                if (opcion == 4) opcion = 5;
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
