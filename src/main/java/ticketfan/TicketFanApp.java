package ticketfan;

import ticketfan.db.Conexion;
import ticketfan.db.IConexion;

/** Primera versión de TicketFan: prueba básica de conexión. */
public class TicketFanApp {
    public static void main(String[] args) {
        IConexion conexion = new Conexion();
        try (var cn = conexion.crearConexion()) {
            System.out.println("TicketFan conectado correctamente a MySQL.");
        } catch (Exception ex) {
            System.out.println("No se pudo conectar: " + ex.getMessage());
        }
    }
}
