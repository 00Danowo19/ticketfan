package ticketfan.model;

/**
 * Información mínima del cliente que inició sesión.
 *
 * @param idCliente identificador del cliente
 * @param nombre nombre visible del cliente
 */
public record ClientSession(int idCliente, String nombre) {}
