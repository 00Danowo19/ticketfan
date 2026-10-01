package ticketfan.ui;

import ticketfan.dao.TicketFanDAO;
import ticketfan.model.AdminSession;
import javax.swing.*;
import java.awt.*;

/** Menú de administrador todavía en desarrollo en esta etapa. */
public class MenuAdministrador extends JFrame {
    public MenuAdministrador() {
        this(null, null);
    }

    public MenuAdministrador(AdminSession session, TicketFanDAO dao) {
        setTitle("TicketFan - Administrador");
        setSize(850, 520);
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
        add(new JLabel("Funciones de administrador en desarrollo", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
