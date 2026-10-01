package ticketfan.ui;

import javax.swing.*;
import java.awt.*;

/** Prototipo inicial del menú del administrador. */
public class MenuAdministrador extends JFrame {
    public MenuAdministrador() {
        setTitle("TicketFan - Administrador");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel menu = new JPanel(new GridLayout(0, 1, 8, 8));
        menu.add(new JButton("Eventos"));
        menu.add(new JButton("Registrar Promotora"));
        menu.add(new JButton("Cuenta Promotora"));
        menu.add(new JButton("Registrar Administrador"));
        menu.add(new JButton("Crear Evento"));
        menu.add(new JButton("Tablero de ventas"));
        add(menu, BorderLayout.WEST);
        add(new JLabel("Contenido del administrador", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
