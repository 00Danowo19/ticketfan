# TicketFan

Aplicación Java SE desarrollada en NetBeans con Maven y JDBC para la gestión y venta de boletos.

## Funciones principales
- Inicio de sesión para clientes y administradores.
- Registro de clientes con cuentas automáticas.
- Consulta y compra de boletos.
- Cancelación de compras y reembolso.
- Generación de boleto en PDF.
- Registro de promotoras, cuentas y administradores.
- Creación y modificación de eventos.
- Dashboard de ventas por evento y general.

## Base de datos
El script se encuentra en `database/TicketFan.sql`.

## Configuración
Copiar `config.example.properties` como `config.properties` y colocar los datos locales de MySQL.
`config.properties` está ignorado por Git para no publicar contraseñas.

## JavaDoc
Abrir `javadoc/index.html`.
