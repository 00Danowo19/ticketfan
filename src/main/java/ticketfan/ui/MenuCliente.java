package ticketfan.ui;

import javax.swing.*;
import java.awt.*;

/** Prototipo inicial del menú del cliente. */
public class MenuCliente extends JFrame {
    public MenuCliente() {
        setTitle("TicketFan - Cliente");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel menu = new JPanel(new GridLayout(0, 1, 8, 8));
        menu.add(new JButton("Mis Cuentas"));
        menu.add(new JButton("Eventos"));
        menu.add(new JButton("Mis Boletos"));
        add(menu, BorderLayout.WEST);
        add(new JLabel("Contenido del cliente", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
