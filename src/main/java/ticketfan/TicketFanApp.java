package ticketfan;

import ticketfan.ui.Login;
import javax.swing.*;

/** Segunda versión: abre el prototipo de interfaz. */
public class TicketFanApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
