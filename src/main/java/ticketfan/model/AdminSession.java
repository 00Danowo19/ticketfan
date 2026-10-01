package ticketfan.model;

/**
 * Información mínima del administrador que inició sesión.
 *
 * @param idAdmin identificador del administrador
 * @param idPromotora identificador de la promotora asociada; puede ser null para un administrador interno
 * @param nombre nombre visible del administrador
 * @param tipoAdmin tipo de administrador, por ejemplo INTERNO o PROMOTORA
 */
public record AdminSession(int idAdmin, Integer idPromotora, String nombre, String tipoAdmin) {}
