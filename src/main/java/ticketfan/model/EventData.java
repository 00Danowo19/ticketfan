package ticketfan.model;

import java.math.BigDecimal;

/**
 * Datos de un evento utilizados para crear o modificar registros.
 *
 * @param idEvento identificador del evento; puede ser null al crear
 * @param idPromotora promotora propietaria
 * @param idTipoEvento categoría del evento
 * @param idCuentaPromotora cuenta receptora
 * @param nombre nombre del evento o show
 * @param edadMinima edad mínima requerida
 * @param imagenPromocion ruta de la imagen promocional
 * @param cantidadBoletos cantidad total de boletos
 * @param precioBoleto precio unitario del boleto
 */
public record EventData(
        Integer idEvento,
        int idPromotora,
        int idTipoEvento,
        int idCuentaPromotora,
        String nombre,
        int edadMinima,
        String imagenPromocion,
        int cantidadBoletos,
        BigDecimal precioBoleto
) {}
