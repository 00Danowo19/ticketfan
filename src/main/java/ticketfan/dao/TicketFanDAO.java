package ticketfan.dao;

import ticketfan.db.IConexion;
import ticketfan.model.AdminSession;
import ticketfan.model.ClientSession;
import ticketfan.model.ComboItem;
import ticketfan.model.EventData;
import ticketfan.model.TicketPrintData;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO principal del proyecto. Contiene las consultas necesarias para las
 * pantallas de administrador y cliente.
 */
public class TicketFanDAO {

    private final IConexion conexion;

    /** Crea el DAO utilizando la implementación de conexión indicada.
     * @param conexion proveedor de conexiones JDBC
     */
    public TicketFanDAO(IConexion conexion) {
        this.conexion = conexion;
    }

    /** Autentica a un administrador.
     * @param usuario nombre de usuario
     * @param password contraseña capturada
     * @return sesión del administrador o {@code null} si las credenciales no coinciden
     * @throws SQLException si ocurre un error de base de datos
     */
    public AdminSession loginAdmin(String usuario, String password) throws SQLException {
        String sql = """
                SELECT id_admin, id_promotora,
                       CONCAT(nombres, ' ', apellido_paterno) AS nombre,
                       tipo_admin
                FROM administrador
                WHERE usuario = ? AND contraseña = SHA2(?, 256)
                """;
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer idPromotora = rs.getObject("id_promotora", Integer.class);
                    return new AdminSession(
                            rs.getInt("id_admin"), idPromotora,
                            rs.getString("nombre"), rs.getString("tipo_admin"));
                }
            }
        }
        return null;
    }

    /** Autentica a un cliente.
     * @param usuario nombre de usuario
     * @param password contraseña capturada
     * @return sesión del cliente o {@code null} si las credenciales no coinciden
     * @throws SQLException si ocurre un error de base de datos
     */
    public ClientSession loginCliente(String usuario, String password) throws SQLException {
        String sql = """
                SELECT id_cliente, CONCAT(nombres, ' ', apellido_paterno) AS nombre
                FROM cliente
                WHERE usuario = ? AND contraseña = SHA2(?, 256)
                """;
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ClientSession(rs.getInt("id_cliente"), rs.getString("nombre"));
                }
            }
        }
        return null;
    }

    /** Indica si ya existe el nombre de usuario de un cliente. */
    public boolean usuarioClienteExiste(String usuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cliente WHERE usuario = ?";
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Obtiene el nombre comercial de una promotora. */
    public String nombrePromotora(int idPromotora) throws SQLException {
        String sql = "SELECT nombre_comercial FROM promotora WHERE id_promotora = ?";
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPromotora);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /** Registra un nuevo cliente. El trigger de la base de datos crea sus cuentas iniciales.
     * @param nombres nombres del cliente
     * @param paterno apellido paterno
     * @param materno apellido materno
     * @param nacimiento fecha de nacimiento
     * @param usuario nombre de usuario
     * @param password contraseña a almacenar mediante SHA-256 en MySQL
     * @throws SQLException si no puede realizarse el registro
     */
    public void registrarCliente(String nombres, String paterno, String materno,
                                 Date nacimiento, String usuario, String password) throws SQLException {
        String sql = """
                INSERT INTO cliente
                (nombres, apellido_paterno, apellido_materno, fecha_nacimiento, usuario, contraseña)
                VALUES (?, ?, ?, ?, ?, SHA2(?, 256))
                """;
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombres);
            ps.setString(2, paterno);
            ps.setString(3, materno);
            ps.setDate(4, nacimiento);
            ps.setString(5, usuario);
            ps.setString(6, password);
            ps.executeUpdate();
        }
    }

    /** Registra un administrador asociado a una promotora. */
    public void registrarAdministrador(int idPromotora, String nombres, String paterno,
                                       String materno, String usuario, String password) throws SQLException {
        if (usuarioAdministradorExiste(usuario)) {
            throw new SQLException("Ya existe un administrador con ese usuario.");
        }

        String sql = """
                INSERT INTO administrador
                (id_promotora, nombres, apellido_paterno, apellido_materno,
                 usuario, contraseña, tipo_admin)
                VALUES (?, ?, ?, ?, ?, SHA2(?, 256), 'PROMOTORA')
                """;

        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPromotora);
            ps.setString(2, nombres);
            ps.setString(3, paterno);
            ps.setString(4, materno);
            ps.setString(5, usuario);
            ps.setString(6, password);
            ps.executeUpdate();
        }
    }

    /** Indica si ya existe el nombre de usuario de un administrador. */
    public boolean usuarioAdministradorExiste(String usuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM administrador WHERE usuario = ?";
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Registra una nueva empresa promotora y devuelve su id. */
    public int registrarPromotora(String nombreComercial, String calle, String numero,
                                  String colonia, String ciudad, String estado) throws SQLException {
        String sql = """
                INSERT INTO promotora
                (nombre_comercial, calle, numero, colonia, ciudad, estado)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreComercial);
            ps.setString(2, calle);
            ps.setString(3, numero);
            ps.setString(4, colonia);
            ps.setString(5, ciudad);
            ps.setString(6, estado);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No se pudo obtener el id de la promotora.");
    }

    /** Registra una cuenta bancaria de promotora con saldo inicial de cero. */
    public void registrarCuentaPromotora(int idPromotora, String banco, String numeroCuenta) throws SQLException {
        String sql = """
                INSERT INTO cuenta_promotora
                (id_promotora, banco, numero_cuenta, saldo)
                VALUES (?, ?, ?, 0.00)
                """;
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPromotora);
            ps.setString(2, banco);
            ps.setString(3, numeroCuenta);
            ps.executeUpdate();
        }
    }

    /** Obtiene el identificador de la compra más reciente del cliente.
     * @param idCliente identificador del cliente
     * @return id de la última compra o 0 si no existen compras
     * @throws SQLException si ocurre un error de consulta
     */
    public int ultimaCompraIdCliente(int idCliente) throws SQLException {
        String sql = "SELECT COALESCE(MAX(id_compra), 0) FROM compra WHERE id_cliente = ?";
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Obtiene los tipos de evento disponibles.
     * @return lista de tipos para controles JComboBox
     * @throws SQLException si ocurre un error de consulta
     */
    public List<ComboItem> tiposEvento() throws SQLException {
        return comboQuery("SELECT id_tipo_evento, nombre FROM tipo_evento ORDER BY nombre");
    }

    /** Registra una nueva categoria/tipo de evento y devuelve su id. */
    public int registrarTipoEvento(String nombre) throws SQLException {
        if (nombre == null || nombre.isBlank()) {
            throw new SQLException("El nombre del tipo de evento es obligatorio.");
        }

        String verificar = "SELECT COUNT(*) FROM tipo_evento WHERE UPPER(nombre) = UPPER(?)";
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(verificar)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    throw new SQLException("Ya existe ese tipo de evento.");
                }
            }
        }

        String sql = "INSERT INTO tipo_evento (nombre) VALUES (?)";
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No se pudo registrar el tipo de evento.");
    }

    /** Obtiene las promotoras registradas.
     * @return lista de promotoras para controles JComboBox
     * @throws SQLException si ocurre un error de consulta
     */
    public List<ComboItem> promotoras() throws SQLException {
        return comboQuery("SELECT id_promotora, nombre_comercial FROM promotora ORDER BY nombre_comercial");
    }

    private List<ComboItem> comboQuery(String sql) throws SQLException {
        List<ComboItem> lista = new ArrayList<>();
        try (Connection cn = this.conexion.crearConexion();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new ComboItem(rs.getInt(1), rs.getString(2)));
            }
        }
        return lista;
    }

    /** Obtiene las cuentas bancarias de una promotora.
     * @param idPromotora identificador de la promotora
     * @return cuentas de la promotora
     * @throws SQLException si ocurre un error de consulta
     */
    public List<ComboItem> cuentasPromotora(int idPromotora) throws SQLException {
        String sql = """
                SELECT id_cuenta_promotora,
                       CONCAT(banco, ' - ', numero_cuenta) AS cuenta
                FROM cuenta_promotora
                WHERE id_promotora = ?
                ORDER BY id_cuenta_promotora
                """;
        List<ComboItem> lista = new ArrayList<>();
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPromotora);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ComboItem(rs.getInt(1), rs.getString(2)));
                }
            }
        }
        return lista;
    }

    /** Consulta todos los datos necesarios para editar un evento.
     * @param idEvento identificador del evento
     * @return datos del evento o {@code null} si no existe
     * @throws SQLException si ocurre un error de consulta
     */
    public EventData obtenerEvento(int idEvento) throws SQLException {
        String sql = """
                SELECT id_evento, id_promotora, id_tipo_evento, id_cuenta_promotora,
                       nombre_evento_show, edad_minima, imagen_promocion,
                       cantidad_boletos, precio_boleto
                FROM evento
                WHERE id_evento = ?
                """;
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new EventData(
                            rs.getInt("id_evento"), rs.getInt("id_promotora"),
                            rs.getInt("id_tipo_evento"), rs.getInt("id_cuenta_promotora"),
                            rs.getString("nombre_evento_show"), rs.getInt("edad_minima"),
                            rs.getString("imagen_promocion"), rs.getInt("cantidad_boletos"),
                            rs.getBigDecimal("precio_boleto"));
                }
            }
        }
        return null;
    }

    /** Registra un evento nuevo; el trigger genera el inventario inicial de boletos.
     * @param e datos del evento
     * @throws SQLException si el evento no puede registrarse
     */
    public void crearEvento(EventData e) throws SQLException {
        String sql = """
                INSERT INTO evento
                (id_promotora, id_tipo_evento, id_cuenta_promotora,
                 nombre_evento_show, edad_minima, imagen_promocion,
                 cantidad_boletos, precio_boleto)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            llenarEvento(ps, e, false);
            ps.executeUpdate();
        }
    }

    /** Actualiza un evento y genera boletos adicionales si aumenta su cantidad.
     * @param e datos actualizados del evento
     * @throws SQLException si la modificación no es válida o falla la actualización
     */
    public void modificarEvento(EventData e) throws SQLException {
        int boletosActuales;
        String countSql = "SELECT COUNT(*) FROM boleto WHERE id_evento = ?";
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(countSql)) {
            ps.setInt(1, e.idEvento());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                boletosActuales = rs.getInt(1);
            }
        }

        if (e.cantidadBoletos() < boletosActuales) {
            throw new SQLException("No se puede reducir la cantidad de boletos ya generados.");
        }

        String sql = """
                UPDATE evento
                SET id_promotora = ?, id_tipo_evento = ?, id_cuenta_promotora = ?,
                    nombre_evento_show = ?, edad_minima = ?, imagen_promocion = ?,
                    cantidad_boletos = ?, precio_boleto = ?
                WHERE id_evento = ?
                """;
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            llenarEvento(ps, e, true);
            ps.executeUpdate();

            if (e.cantidadBoletos() > boletosActuales) {
                String insert = """
                        INSERT INTO boleto (id_evento, folio, codigo_qr, estado)
                        VALUES (?, ?, ?, 'DISPONIBLE')
                        """;
                try (PreparedStatement p2 = cn.prepareStatement(insert)) {
                    for (int i = boletosActuales + 1; i <= e.cantidadBoletos(); i++) {
                        p2.setInt(1, e.idEvento());
                        p2.setString(2, "EV-" + e.idEvento() + "-" + i);
                        p2.setString(3, "QR-" + e.idEvento() + "-" + i);
                        p2.addBatch();
                    }
                    p2.executeBatch();
                }
            }
        }
    }

    private void llenarEvento(PreparedStatement ps, EventData e, boolean incluirId) throws SQLException {
        ps.setInt(1, e.idPromotora());
        ps.setInt(2, e.idTipoEvento());
        ps.setInt(3, e.idCuentaPromotora());
        ps.setString(4, e.nombre());
        ps.setInt(5, e.edadMinima());
        ps.setString(6, e.imagenPromocion());
        ps.setInt(7, e.cantidadBoletos());
        ps.setBigDecimal(8, e.precioBoleto());
        if (incluirId) ps.setInt(9, e.idEvento());
    }

    /** Consulta eventos para la vista administrativa.
     * @param idPromotora promotora a filtrar; {@code null} para el administrador interno
     * @return filas de eventos para la tabla administrativa
     * @throws SQLException si ocurre un error de consulta
     */
    public List<Object[]> eventosAdmin(Integer idPromotora) throws SQLException {
        String sql = """
                SELECT e.id_evento, e.nombre_evento_show, te.nombre,
                       e.edad_minima, e.cantidad_boletos, e.precio_boleto,
                       cp.banco, cp.numero_cuenta, p.nombre_comercial
                FROM evento e
                INNER JOIN tipo_evento te ON e.id_tipo_evento = te.id_tipo_evento
                INNER JOIN cuenta_promotora cp ON e.id_cuenta_promotora = cp.id_cuenta_promotora
                INNER JOIN promotora p ON e.id_promotora = p.id_promotora
                """ + (idPromotora == null ? "" : " WHERE e.id_promotora = ? ") +
                " ORDER BY e.id_evento";
        return queryRows(sql, idPromotora);
    }

    /** Obtiene las métricas de ventas por evento.
     * @param idPromotora promotora a filtrar; {@code null} para ver todas
     * @return filas del tablero de ventas
     * @throws SQLException si ocurre un error de consulta
     */
    public List<Object[]> dashboardEventos(Integer idPromotora) throws SQLException {
        String sql = """
                SELECT v.id_evento, v.nombre_evento_show, v.promotora,
                       v.precio_boleto, v.boletos_totales, v.boletos_vendidos,
                       v.boletos_por_vender, v.dinero_vendido,
                       v.dinero_por_vender, v.valor_total_evento
                FROM vw_ventas_por_evento v
                INNER JOIN evento e ON v.id_evento = e.id_evento
                """ + (idPromotora == null ? "" : " WHERE e.id_promotora = ? ") +
                " ORDER BY v.id_evento";
        return queryRows(sql, idPromotora);
    }

    /** Obtiene cuentas y saldos de promotoras para el tablero.
     * @param idPromotora promotora a filtrar; {@code null} para ver todas
     * @return filas con promotora, banco, número de cuenta y saldo
     * @throws SQLException si ocurre un error de consulta
     */
    public List<Object[]> cuentasPromotoraTabla(Integer idPromotora) throws SQLException {
        String sql = """
                SELECT p.nombre_comercial, cp.banco, cp.numero_cuenta, cp.saldo
                FROM cuenta_promotora cp
                INNER JOIN promotora p ON cp.id_promotora = p.id_promotora
                """ + (idPromotora == null ? "" : " WHERE cp.id_promotora = ? ") +
                " ORDER BY p.nombre_comercial, cp.id_cuenta_promotora";
        return queryRows(sql, idPromotora);
    }

    /** Calcula las ganancias de compras activas.
     * @param idPromotora promotora a filtrar; {@code null} para total general
     * @return importe total vendido
     * @throws SQLException si ocurre un error de consulta
     */
    public BigDecimal gananciasGenerales(Integer idPromotora) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(c.precio_final), 0)
                FROM compra c
                INNER JOIN boleto b ON c.id_boleto = b.id_boleto
                INNER JOIN evento e ON b.id_evento = e.id_evento
                WHERE c.estado = 'COMPRADO'
                """ + (idPromotora == null ? "" : " AND e.id_promotora = ?");
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (idPromotora != null) ps.setInt(1, idPromotora);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    /** Obtiene los eventos visibles para los clientes.
     * @return filas con información y disponibilidad de eventos
     * @throws SQLException si ocurre un error de consulta
     */
    public List<Object[]> eventosCliente() throws SQLException {
        String sql = """
                SELECT e.id_evento, e.nombre_evento_show, te.nombre,
                       e.edad_minima,
                       SUM(CASE WHEN b.estado = 'DISPONIBLE' THEN 1 ELSE 0 END) AS disponibles,
                       e.precio_boleto, p.nombre_comercial
                FROM evento e
                INNER JOIN tipo_evento te ON e.id_tipo_evento = te.id_tipo_evento
                INNER JOIN promotora p ON e.id_promotora = p.id_promotora
                LEFT JOIN boleto b ON e.id_evento = b.id_evento
                GROUP BY e.id_evento, e.nombre_evento_show, te.nombre,
                         e.edad_minima, e.precio_boleto, p.nombre_comercial
                ORDER BY e.id_evento
                """;
        return queryRows(sql, null);
    }

    /** Obtiene los boletos disponibles de un evento.
     * @param idEvento identificador del evento
     * @return boletos disponibles
     * @throws SQLException si ocurre un error de consulta
     */
    public List<ComboItem> boletosDisponibles(int idEvento) throws SQLException {
        String sql = """
                SELECT id_boleto, folio
                FROM boleto
                WHERE id_evento = ? AND estado = 'DISPONIBLE'
                ORDER BY id_boleto
                """;
        List<ComboItem> lista = new ArrayList<>();
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(new ComboItem(rs.getInt(1), rs.getString(2)));
            }
        }
        return lista;
    }

    /** Obtiene las cuentas del cliente para seleccionar una forma de pago.
     * @param idCliente identificador del cliente
     * @return cuentas del cliente con banco, número y saldo
     * @throws SQLException si ocurre un error de consulta
     */
    public List<ComboItem> cuentasClienteCombo(int idCliente) throws SQLException {
        String sql = """
                SELECT id_cuenta_cliente,
                       CONCAT(banco, ' - ', numero_cuenta, ' - $', FORMAT(saldo, 2))
                FROM cuenta_cliente
                WHERE id_cliente = ?
                ORDER BY id_cuenta_cliente
                """;
        List<ComboItem> lista = new ArrayList<>();
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(new ComboItem(rs.getInt(1), rs.getString(2)));
            }
        }
        return lista;
    }

    /** Consulta las cuentas del cliente para mostrarlas en una tabla.
     * @param idCliente identificador del cliente
     * @return filas con banco, número de cuenta y saldo
     * @throws SQLException si ocurre un error de consulta
     */
    public List<Object[]> cuentasClienteTabla(int idCliente) throws SQLException {
        String sql = """
                SELECT banco, numero_cuenta, saldo
                FROM cuenta_cliente
                WHERE id_cliente = ?
                ORDER BY id_cuenta_cliente
                """;
        return queryRows(sql, idCliente);
    }

    /** Obtiene el precio actual del boleto de un evento.
     * @param idEvento identificador del evento
     * @return precio del boleto o cero si el evento no existe
     * @throws SQLException si ocurre un error de consulta
     */
    public BigDecimal precioEvento(int idEvento) throws SQLException {
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT precio_boleto FROM evento WHERE id_evento = ?")) {
            ps.setInt(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    /** Ejecuta el procedimiento almacenado de compra de boleto.
     * @param idCliente cliente comprador
     * @param idBoleto boleto seleccionado
     * @param idCuentaCliente cuenta desde la que se realiza el pago
     * @param precio precio final de la compra
     * @throws SQLException si la compra incumple una validación o falla en MySQL
     */
    public void comprarBoleto(int idCliente, int idBoleto, int idCuentaCliente, BigDecimal precio) throws SQLException {
        try (Connection cn = this.conexion.crearConexion();
             CallableStatement cs = cn.prepareCall("{CALL sp_comprar_boleto(?,?,?,?)}")) {
            cs.setInt(1, idCliente);
            cs.setInt(2, idBoleto);
            cs.setInt(3, idCuentaCliente);
            cs.setBigDecimal(4, precio);
            cs.execute();
        }
    }

    /** Obtiene el historial de compras de un cliente.
     * @param idCliente identificador del cliente
     * @return filas del historial ordenadas de la más reciente a la más antigua
     * @throws SQLException si ocurre un error de consulta
     */
    public List<Object[]> historialCompras(int idCliente) throws SQLException {
        String sql = """
                SELECT c.id_compra, e.nombre_evento_show, b.folio,
                       c.fecha_compra, c.precio_final, c.estado
                FROM compra c
                INNER JOIN boleto b ON c.id_boleto = b.id_boleto
                INNER JOIN evento e ON b.id_evento = e.id_evento
                WHERE c.id_cliente = ?
                ORDER BY c.fecha_compra DESC
                """;
        return queryRows(sql, idCliente);
    }

    /** Ejecuta el procedimiento almacenado de cancelación y reembolso.
     * @param idCompra identificador de la compra
     * @throws SQLException si no puede cancelarse o falla la operación
     */
    public void cancelarCompra(int idCompra) throws SQLException {
        try (Connection cn = this.conexion.crearConexion();
             CallableStatement cs = cn.prepareCall("{CALL sp_cancelar_compra(?)}")) {
            cs.setInt(1, idCompra);
            cs.execute();
        }
    }

    /** Obtiene los datos de un boleto perteneciente al cliente para generar su PDF.
     * @param idCompra identificador de la compra
     * @param idCliente identificador del cliente dueño de la compra
     * @return datos del boleto o {@code null} si la compra no pertenece al cliente
     * @throws SQLException si ocurre un error de consulta
     */
    public TicketPrintData boletoParaImprimir(int idCompra, int idCliente) throws SQLException {
        String sql = """
                SELECT c.id_compra,
                       CONCAT(cl.nombres, ' ', cl.apellido_paterno, ' ', cl.apellido_materno) AS cliente,
                       e.nombre_evento_show,
                       b.folio, b.codigo_qr, c.fecha_compra, c.precio_final, c.estado
                FROM compra c
                INNER JOIN cliente cl ON c.id_cliente = cl.id_cliente
                INNER JOIN boleto b ON c.id_boleto = b.id_boleto
                INNER JOIN evento e ON b.id_evento = e.id_evento
                WHERE c.id_compra = ? AND c.id_cliente = ?
                """;
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            ps.setInt(2, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TicketPrintData(
                            rs.getInt("id_compra"), rs.getString("cliente"),
                            rs.getString("nombre_evento_show"), rs.getString("folio"),
                            rs.getString("codigo_qr"), rs.getTimestamp("fecha_compra"),
                            rs.getBigDecimal("precio_final"), rs.getString("estado"));
                }
            }
        }
        return null;
    }

    private List<Object[]> queryRows(String sql, Integer parameter) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        try (Connection cn = this.conexion.crearConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (parameter != null) ps.setInt(1, parameter);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int columns = md.getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[columns];
                    for (int i = 1; i <= columns; i++) row[i - 1] = rs.getObject(i);
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
