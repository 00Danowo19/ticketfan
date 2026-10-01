package ticketfan.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Datos necesarios para generar el boleto PDF del cliente.
 *
 * @param idCompra identificador de la compra
 * @param cliente nombre del cliente
 * @param evento nombre del evento
 * @param folio folio del boleto
 * @param codigoQr código identificador del boleto
 * @param fechaCompra fecha y hora de la compra
 * @param precio precio final pagado
 * @param estado estado de la compra
 */
public record TicketPrintData(
        int idCompra,
        String cliente,
        String evento,
        String folio,
        String codigoQr,
        Timestamp fechaCompra,
        BigDecimal precio,
        String estado
) {}
