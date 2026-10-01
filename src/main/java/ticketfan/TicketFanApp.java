package ticketfan;

import ticketfan.dao.TicketFanDAO;
import ticketfan.db.Conexion;
import ticketfan.db.IConexion;
import ticketfan.ui.Login;

import javax.swing.*;

/**
 * Punto de entrada de la aplicación TicketFan.
 *
 * @author Jesus Daniel Peralta Grajeda
 */
public class TicketFanApp {
    /**
     * Inicializa la conexión, el DAO y muestra la pantalla de inicio de sesión.
     *
     * @param args argumentos de línea de comandos; no se utilizan
     */
    public static void main(String[] args) {

        IConexion conexion = new Conexion();
        TicketFanDAO dao = new TicketFanDAO(conexion);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            try (var cn = conexion.crearConexion()) {
                new Login(dao).setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "No se pudo conectar a MySQL.\n\n" + ex.getMessage()
                                + "\n\nRevisa db.url, db.usuario y db.contrasena en config.properties.",
                        "TicketFan - Error de conexion",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
