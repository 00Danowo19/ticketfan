package ticketfan.db;

import java.sql.Connection;
import java.sql.SQLException;

/** Contrato para crear conexiones a la base de datos. */
public interface IConexion {
    /**
     * Crea y devuelve una conexión activa a la base de datos TicketFan.
     *
     * @return conexión JDBC activa
     * @throws SQLException si no es posible establecer la conexión
     */
    Connection crearConexion() throws SQLException;
}
