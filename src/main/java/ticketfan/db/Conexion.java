package ticketfan.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Implementación sencilla de la conexión JDBC a TicketFan. */
public class Conexion implements IConexion {

    private static final String ARCHIVO_CONFIG = "config.properties";

    /**
     * Crea una conexión JDBC usando los valores definidos en config.properties.
     *
     * @return conexión activa a MySQL
     * @throws SQLException si falta la configuración o falla la conexión
     */
    @Override
    public Connection crearConexion() throws SQLException {
        Properties p = cargarConfiguracion();
        String url = p.getProperty("db.url",
                "jdbc:mysql://localhost:3306/TicketFan?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Hermosillo");
        String usuario = p.getProperty("db.usuario", "root");
        String contrasena = p.getProperty("db.contrasena", "");
        System.out.println("Se creo la conexion");
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    private Properties cargarConfiguracion() throws SQLException {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(ARCHIVO_CONFIG)) {
            p.load(in);
        } catch (IOException ex) {
            throw new SQLException("No se encontró config.properties. Copia config.example.properties, renómbralo y coloca tus datos de MySQL.", ex);
        }
        return p;
    }
}
