package ticketfan.model;

/**
 * Elemento sencillo para JComboBox que conserva un identificador y una etiqueta visible.
 *
 * @param id identificador del registro
 * @param label texto mostrado en el control
 */
public record ComboItem(int id, String label) {
    @Override
    public String toString() {
        return label;
    }
}
