package ticketfan.ui;

import javax.swing.*;
import java.awt.*;

/** Prototipo inicial de inicio de sesión, aún sin autenticación real. */
public class Login extends JFrame {
    public Login() {
        setTitle("TicketFan - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 280);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        JTextField usuario = new JTextField();
        JPasswordField password = new JPasswordField();
        JButton entrar = new JButton("Iniciar Sesion");
        JButton registrar = new JButton("Registrarse");
        panel.add(new JLabel("TicketFan"));
        panel.add(new JLabel("Usuario"));
        panel.add(usuario);
        panel.add(new JLabel("Contraseña"));
        panel.add(password);
        panel.add(entrar);
        panel.add(registrar);
        add(panel);

        entrar.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Inicio de sesión todavía no implementado."));
    }
}
