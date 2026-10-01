-- ============================================================
-- TICKETFAN
-- Script sencillo alineado con el MER y storyboard actuales
-- ============================================================

DROP DATABASE IF EXISTS TicketFan;
CREATE DATABASE TicketFan;
USE TicketFan;

-- ============================================================
-- 1. TABLAS
-- ============================================================

CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(80) NOT NULL,
    apellido_materno VARCHAR(80) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    usuario VARCHAR(60) NOT NULL UNIQUE,
    contraseña VARCHAR(255) NOT NULL
);

CREATE TABLE promotora (
    id_promotora INT AUTO_INCREMENT PRIMARY KEY,
    nombre_comercial VARCHAR(120) NOT NULL UNIQUE,
    calle VARCHAR(120) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    colonia VARCHAR(100) NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    estado VARCHAR(100) NOT NULL
);

CREATE TABLE administrador (
    id_admin INT AUTO_INCREMENT PRIMARY KEY,
    id_promotora INT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(80) NOT NULL,
    apellido_materno VARCHAR(80) NOT NULL,
    usuario VARCHAR(60) NOT NULL UNIQUE,
    contraseña VARCHAR(255) NOT NULL,
    tipo_admin VARCHAR(20) NOT NULL,

    FOREIGN KEY (id_promotora)
        REFERENCES promotora(id_promotora)
);

CREATE TABLE cuenta_cliente (
    id_cuenta_cliente INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    banco ENUM('BBVA', 'BANCOPPEL', 'PLATA') NOT NULL,
    numero_cuenta VARCHAR(20) NOT NULL UNIQUE,
    saldo DECIMAL(12,2) NOT NULL DEFAULT 1000.00,

    FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente)
);

CREATE TABLE cuenta_promotora (
    id_cuenta_promotora INT AUTO_INCREMENT PRIMARY KEY,
    id_promotora INT NOT NULL,
    banco ENUM('BBVA', 'BANCOPPEL', 'PLATA') NOT NULL,
    numero_cuenta VARCHAR(20) NOT NULL UNIQUE,
    saldo DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    FOREIGN KEY (id_promotora)
        REFERENCES promotora(id_promotora)
);

CREATE TABLE tipo_evento (
    id_tipo_evento INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE evento (
    id_evento INT AUTO_INCREMENT PRIMARY KEY,
    id_promotora INT NOT NULL,
    id_tipo_evento INT NOT NULL,
    id_cuenta_promotora INT NOT NULL,
    nombre_evento_show VARCHAR(150) NOT NULL,
    edad_minima INT DEFAULT 0,
    imagen_promocion VARCHAR(255),
    cantidad_boletos INT NOT NULL,
    precio_boleto DECIMAL(10,2) NOT NULL,

    FOREIGN KEY (id_promotora)
        REFERENCES promotora(id_promotora),

    FOREIGN KEY (id_tipo_evento)
        REFERENCES tipo_evento(id_tipo_evento),

    FOREIGN KEY (id_cuenta_promotora)
        REFERENCES cuenta_promotora(id_cuenta_promotora)
);

CREATE TABLE boleto (
    id_boleto INT AUTO_INCREMENT PRIMARY KEY,
    id_evento INT NOT NULL,
    folio VARCHAR(60) NOT NULL UNIQUE,
    codigo_qr VARCHAR(120) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',

    FOREIGN KEY (id_evento)
        REFERENCES evento(id_evento)
);

CREATE TABLE compra (
    id_compra INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_boleto INT NOT NULL,
    id_cuenta_cliente INT NOT NULL,
    fecha_compra DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    precio_final DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'COMPRADO',
    fecha_cancelacion DATETIME NULL,

    FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente),

    FOREIGN KEY (id_boleto)
        REFERENCES boleto(id_boleto),

    FOREIGN KEY (id_cuenta_cliente)
        REFERENCES cuenta_cliente(id_cuenta_cliente)
);

CREATE TABLE operacion_cuenta (
    id_operacion INT AUTO_INCREMENT PRIMARY KEY,
    id_cuenta_cliente INT NOT NULL,
    tipo_operacion VARCHAR(20) NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descripcion VARCHAR(255),

    FOREIGN KEY (id_cuenta_cliente)
        REFERENCES cuenta_cliente(id_cuenta_cliente)
);

-- ============================================================
-- 2. TRIGGERS
-- ============================================================

DELIMITER $$

-- Al registrar un cliente se crean 3 cuentas aleatorias con $1,000.
CREATE TRIGGER trg_cliente_crear_cuentas
AFTER INSERT ON cliente
FOR EACH ROW
BEGIN
    INSERT INTO cuenta_cliente
        (id_cliente, banco, numero_cuenta, saldo)
    VALUES
        (
            NEW.id_cliente,
            ELT(FLOOR(1 + RAND() * 3), 'BBVA', 'BANCOPPEL', 'PLATA'),
            CAST(FLOOR(100000000000 + RAND() * 900000000000) AS CHAR),
            1000.00
        ),
        (
            NEW.id_cliente,
            ELT(FLOOR(1 + RAND() * 3), 'BBVA', 'BANCOPPEL', 'PLATA'),
            CAST(FLOOR(100000000000 + RAND() * 900000000000) AS CHAR),
            1000.00
        ),
        (
            NEW.id_cliente,
            ELT(FLOOR(1 + RAND() * 3), 'BBVA', 'BANCOPPEL', 'PLATA'),
            CAST(FLOOR(100000000000 + RAND() * 900000000000) AS CHAR),
            1000.00
        );
END$$

-- Al crear un evento se genera automáticamente su inventario de boletos.
CREATE TRIGGER trg_evento_generar_boletos
AFTER INSERT ON evento
FOR EACH ROW
BEGIN
    DECLARE contador INT DEFAULT 1;

    WHILE contador <= NEW.cantidad_boletos DO
        INSERT INTO boleto
            (id_evento, folio, codigo_qr, estado)
        VALUES
            (
                NEW.id_evento,
                CONCAT('EV-', NEW.id_evento, '-', contador),
                CONCAT('QR-', NEW.id_evento, '-', contador),
                'DISPONIBLE'
            );

        SET contador = contador + 1;
    END WHILE;
END$$

-- ============================================================
-- 3. PROCEDIMIENTO: COMPRAR BOLETO
-- ============================================================

CREATE PROCEDURE sp_comprar_boleto(
    IN p_cliente INT,
    IN p_boleto INT,
    IN p_cuenta_cliente INT,
    IN p_precio DECIMAL(10,2)
)
BEGIN
    DECLARE saldo_cliente DECIMAL(12,2) DEFAULT NULL;
    DECLARE estado_boleto VARCHAR(20) DEFAULT NULL;
    DECLARE cuenta_destino INT DEFAULT NULL;
    DECLARE compra_activa INT DEFAULT 0;

    -- Verificar que la cuenta pertenezca al cliente.
    SELECT saldo
    INTO saldo_cliente
    FROM cuenta_cliente
    WHERE id_cuenta_cliente = p_cuenta_cliente
      AND id_cliente = p_cliente
    FOR UPDATE;

    IF saldo_cliente IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La cuenta no pertenece al cliente';
    END IF;

    IF p_precio <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El precio debe ser mayor que cero';
    END IF;

    IF saldo_cliente < p_precio THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Saldo insuficiente';
    END IF;

    -- Saber si el boleto está disponible y a qué cuenta de promotora va el dinero.
    SELECT b.estado, e.id_cuenta_promotora
    INTO estado_boleto, cuenta_destino
    FROM boleto b
    INNER JOIN evento e
        ON b.id_evento = e.id_evento
    WHERE b.id_boleto = p_boleto
    FOR UPDATE;

    IF estado_boleto IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El boleto no existe';
    END IF;

    IF estado_boleto <> 'DISPONIBLE' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El boleto ya fue comprado';
    END IF;

    SELECT COUNT(*)
    INTO compra_activa
    FROM compra
    WHERE id_boleto = p_boleto
      AND estado = 'COMPRADO';

    IF compra_activa > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No se puede comprar dos veces el mismo boleto';
    END IF;

    -- Quitar dinero de la cuenta del cliente.
    UPDATE cuenta_cliente
    SET saldo = saldo - p_precio
    WHERE id_cuenta_cliente = p_cuenta_cliente;

    -- Depositar el dinero en la cuenta receptora del evento.
    UPDATE cuenta_promotora
    SET saldo = saldo + p_precio
    WHERE id_cuenta_promotora = cuenta_destino;

    -- Marcar el boleto como comprado.
    UPDATE boleto
    SET estado = 'COMPRADO'
    WHERE id_boleto = p_boleto;

    -- Registrar la compra.
    INSERT INTO compra
        (id_cliente, id_boleto, id_cuenta_cliente, precio_final, estado)
    VALUES
        (p_cliente, p_boleto, p_cuenta_cliente, p_precio, 'COMPRADO');

    -- Registrar el movimiento de la cuenta del cliente.
    INSERT INTO operacion_cuenta
        (id_cuenta_cliente, tipo_operacion, monto, descripcion)
    VALUES
        (
            p_cuenta_cliente,
            'CARGO',
            p_precio,
            CONCAT('Compra del boleto ', p_boleto)
        );

END$$

-- ============================================================
-- 4. PROCEDIMIENTO: CANCELAR COMPRA
-- ============================================================

CREATE PROCEDURE sp_cancelar_compra(
    IN p_compra INT
)
BEGIN
    DECLARE boleto_compra INT DEFAULT NULL;
    DECLARE cuenta_cliente_compra INT DEFAULT NULL;
    DECLARE cuenta_promotora_compra INT DEFAULT NULL;
    DECLARE precio_compra DECIMAL(10,2) DEFAULT NULL;
    DECLARE fecha_original DATETIME DEFAULT NULL;
    DECLARE estado_compra VARCHAR(20) DEFAULT NULL;

    SELECT
        id_boleto,
        id_cuenta_cliente,
        precio_final,
        fecha_compra,
        estado
    INTO
        boleto_compra,
        cuenta_cliente_compra,
        precio_compra,
        fecha_original,
        estado_compra
    FROM compra
    WHERE id_compra = p_compra
    FOR UPDATE;

    IF estado_compra IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La compra no existe';
    END IF;

    IF estado_compra <> 'COMPRADO' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La compra ya esta cancelada';
    END IF;

    -- 24 horas exactas = 86,400 segundos.
    IF TIMESTAMPDIFF(SECOND, fecha_original, NOW()) > 86400 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No se puede cancelar despues de 24 horas';
    END IF;

    -- Obtener la cuenta receptora del evento.
    SELECT e.id_cuenta_promotora
    INTO cuenta_promotora_compra
    FROM boleto b
    INNER JOIN evento e
        ON b.id_evento = e.id_evento
    WHERE b.id_boleto = boleto_compra;

    -- Cancelar compra y liberar boleto.
    UPDATE compra
    SET estado = 'CANCELADO',
        fecha_cancelacion = NOW()
    WHERE id_compra = p_compra;

    UPDATE boleto
    SET estado = 'DISPONIBLE'
    WHERE id_boleto = boleto_compra;

    -- Regresar el dinero al cliente.
    UPDATE cuenta_cliente
    SET saldo = saldo + precio_compra
    WHERE id_cuenta_cliente = cuenta_cliente_compra;

    -- Retirar de la cuenta receptora de la promotora.
    UPDATE cuenta_promotora
    SET saldo = saldo - precio_compra
    WHERE id_cuenta_promotora = cuenta_promotora_compra;

    -- Registrar reembolso.
    INSERT INTO operacion_cuenta
        (id_cuenta_cliente, tipo_operacion, monto, descripcion)
    VALUES
        (
            cuenta_cliente_compra,
            'REEMBOLSO',
            precio_compra,
            CONCAT('Cancelacion de compra ', p_compra)
        );

END$$

DELIMITER ;

-- ============================================================
-- 5. DATOS DE PRUEBA
-- ============================================================

INSERT INTO tipo_evento (nombre)
VALUES
('Concierto'),
('Festival'),
('Stand Up');

INSERT INTO promotora
    (nombre_comercial, calle, numero, colonia, ciudad, estado)
VALUES
('Eventos del Yaqui', 'Miguel Aleman', '100', 'Centro', 'Ciudad Obregon', 'Sonora'),
('Potros Producciones', '5 de Febrero', '200', 'Centro', 'Ciudad Obregon', 'Sonora');

-- Dos administradores por promotora y un administrador interno.
INSERT INTO administrador
    (id_promotora, nombres, apellido_paterno, apellido_materno, usuario, contraseña, tipo_admin)
VALUES
(1, 'Ana', 'Lopez', 'Ruiz', 'ana.yaqui', SHA2('Admin123', 256), 'PROMOTORA'),
(1, 'Luis', 'Perez', 'Mora', 'luis.yaqui', SHA2('Admin123', 256), 'PROMOTORA'),
(2, 'Marta', 'Garcia', 'Leon', 'marta.potros', SHA2('Admin123', 256), 'PROMOTORA'),
(2, 'Carlos', 'Diaz', 'Soto', 'carlos.potros', SHA2('Admin123', 256), 'PROMOTORA'),
(NULL, 'Sofia', 'Torres', 'Vega', 'admin.ticketfan', SHA2('Admin123', 256), 'INTERNO');

-- Dos cuentas por promotora con saldo inicial de $0.
INSERT INTO cuenta_promotora
    (id_promotora, banco, numero_cuenta, saldo)
VALUES
(1, 'BBVA',      '700100000001', 0.00),
(1, 'PLATA',     '700100000002', 0.00),
(2, 'BANCOPPEL', '700200000001', 0.00),
(2, 'BBVA',      '700200000002', 0.00);

-- Al insertar el cliente, el trigger crea sus 3 cuentas aleatorias.
INSERT INTO cliente
    (nombres, apellido_paterno, apellido_materno, fecha_nacimiento, usuario, contraseña)
VALUES
('Fernanda', 'Lopez', 'Garcia', '2003-05-10', 'fernanda01', SHA2('Cliente123', 256));

-- Cada evento indica qué cuenta de su promotora recibirá el dinero.
INSERT INTO evento
    (id_promotora, id_tipo_evento, id_cuenta_promotora,
     nombre_evento_show, edad_minima, imagen_promocion, cantidad_boletos, precio_boleto)
VALUES
(1, 1, 1, 'Concierto Rock', 18, 'concierto_rock.jpg', 20, 150.00),
(2, 2, 3, 'Festival Pop', 0, 'festival_pop.jpg', 15, 200.00),
(1, 3, 2, 'Stand Up Night', 18, 'stand_up_night.jpg', 10, 120.00);

-- ============================================================
-- 6. VISTAS PARA LOS TABLEROS
-- ============================================================

CREATE VIEW vw_ventas_por_evento AS
SELECT
    e.id_evento,
    e.nombre_evento_show,
    p.nombre_comercial AS promotora,
    e.precio_boleto,
    e.cantidad_boletos AS boletos_totales,

    COUNT(
        CASE
            WHEN c.estado = 'COMPRADO' THEN 1
        END
    ) AS boletos_vendidos,

    e.cantidad_boletos -
    COUNT(
        CASE
            WHEN c.estado = 'COMPRADO' THEN 1
        END
    ) AS boletos_por_vender,

    COALESCE(
        SUM(
            CASE
                WHEN c.estado = 'COMPRADO'
                THEN c.precio_final
                ELSE 0
            END
        ),
        0
    ) AS dinero_vendido,

    (
        e.cantidad_boletos -
        COUNT(
            CASE
                WHEN c.estado = 'COMPRADO' THEN 1
            END
        )
    ) * e.precio_boleto AS dinero_por_vender,

    e.cantidad_boletos * e.precio_boleto AS valor_total_evento

FROM evento e

INNER JOIN promotora p
    ON e.id_promotora = p.id_promotora

LEFT JOIN boleto b
    ON e.id_evento = b.id_evento

LEFT JOIN compra c
    ON b.id_boleto = c.id_boleto

GROUP BY
    e.id_evento,
    e.nombre_evento_show,
    p.nombre_comercial,
    e.precio_boleto,
    e.cantidad_boletos;

CREATE VIEW vw_ventas_generales AS
SELECT
    COUNT(CASE WHEN estado = 'COMPRADO' THEN 1 END) AS compras_activas,
    COUNT(CASE WHEN estado = 'CANCELADO' THEN 1 END) AS compras_canceladas,
    COALESCE(
        SUM(CASE WHEN estado = 'COMPRADO' THEN precio_final ELSE 0 END),
        0
    ) AS ventas_totales
FROM compra;

CREATE VIEW vw_cuentas_promotora AS
SELECT
    p.id_promotora,
    p.nombre_comercial,
    cp.id_cuenta_promotora,
    cp.banco,
    cp.numero_cuenta,
    cp.saldo
FROM promotora p
INNER JOIN cuenta_promotora cp
    ON p.id_promotora = cp.id_promotora;

-- ============================================================
-- 7. CONSULTAS DE PRUEBA
-- ============================================================

SELECT * FROM cliente;
SELECT * FROM cuenta_cliente;
SELECT * FROM promotora;
SELECT * FROM cuenta_promotora;
SELECT * FROM administrador;
SELECT * FROM tipo_evento;
SELECT * FROM evento;
SELECT * FROM boleto;
SELECT * FROM compra;
SELECT * FROM operacion_cuenta;

-- Ver las cuentas del cliente que inició sesión (ejemplo: id_cliente = 1).
SELECT *
FROM cuenta_cliente
WHERE id_cliente = 1;

-- Saber a qué cuenta de promotora va el dinero de cada evento.
SELECT
    e.id_evento,
    e.nombre_evento_show,
    p.nombre_comercial,
    cp.banco,
    cp.numero_cuenta
FROM evento e
INNER JOIN promotora p
    ON e.id_promotora = p.id_promotora
INNER JOIN cuenta_promotora cp
    ON e.id_cuenta_promotora = cp.id_cuenta_promotora;

-- Saber qué administradores tiene cada promotora.
SELECT
    p.nombre_comercial,
    a.nombres,
    a.apellido_paterno,
    a.apellido_materno
FROM promotora p
INNER JOIN administrador a
    ON p.id_promotora = a.id_promotora;

-- Historial de compras del cliente.
SELECT
    c.id_compra,
    e.nombre_evento_show,
    b.folio,
    c.fecha_compra,
    c.precio_final,
    c.estado
FROM compra c
INNER JOIN boleto b
    ON c.id_boleto = b.id_boleto
INNER JOIN evento e
    ON b.id_evento = e.id_evento
WHERE c.id_cliente = 1;

-- Tableros.
SELECT * FROM vw_ventas_por_evento;
SELECT * FROM vw_ventas_generales;

-- ============================================================
-- 8. PRUEBAS DE PROCEDIMIENTOS
-- ============================================================

-- Primero consulta las cuentas creadas para el cliente:
-- SELECT * FROM cuenta_cliente WHERE id_cliente = 1;

-- Si la primera cuenta del cliente es id_cuenta_cliente = 1:
-- CALL sp_comprar_boleto(1, 1, 1, 150.00);

-- Ver compra y saldos:
-- SELECT * FROM compra;
-- SELECT * FROM cuenta_cliente;
-- SELECT * FROM cuenta_promotora;

-- Cancelar compra 1:
-- CALL sp_cancelar_compra(1);

-- Ver saldo después del reembolso:
-- SELECT * FROM cuenta_cliente;
-- SELECT * FROM cuenta_promotora;


-- Dashboard detallado por evento
SELECT * FROM vw_ventas_por_evento;

-- Cuentas y saldos de cada promotora para el dashboard general
SELECT * FROM vw_cuentas_promotora;


-- Ver precio configurado para cada evento
SELECT
    id_evento,
    nombre_evento_show,
    precio_boleto
FROM evento;
