# Pruebas rápidas - TicketFan

## 1. Base de datos
Ejecuta `database/TicketFan.sql` completo en MySQL Workbench.

## 2. Conexión local
La conexión se lee desde `config.properties`.
Ese archivo está ignorado por Git y no debe subirse al repositorio público.

## 3. Usuarios de prueba
### Administrador de promotora
- usuario: `ana.yaqui`
- contraseña: `Admin123`
- seleccionar: **Administrador**

### Administrador interno
- usuario: `admin.ticketfan`
- contraseña: `Admin123`
- seleccionar: **Administrador**

### Cliente
- usuario: `fernanda01`
- contraseña: `Cliente123`
- seleccionar: **Cliente**

## 4. Flujo cliente
1. Entrar como cliente.
2. Eventos -> seleccionar fila -> Ver.
3. Comprar -> seleccionar boleto -> Seleccionar.
4. Elegir cuenta -> Confirmar compra.
5. Revisar éxito y generar PDF.
6. Mis Boletos -> seleccionar compra -> Cancelar.
7. Confirmar cancelación dentro de 24 horas.
8. Mis Cuentas -> comprobar saldo/reembolso.

## 5. Flujo administrador de promotora
1. Entrar como administrador de promotora.
2. Eventos -> consultar solo eventos de su empresa.
3. Crear evento -> seleccionar tipo, cuenta receptora, cantidad y precio.
4. Modificar -> seleccionar un evento -> Modificar -> Guardar Cambios.
5. Cuenta Promotora -> registrar cuenta.
6. Registrar Administrador -> registrar otro admin de la misma empresa.
7. Tablero -> revisar boletos totales/vendidos/por vender, dinero vendido/por vender, valor total y saldos.

## 6. Flujo administrador interno
1. Entrar con `admin.ticketfan`.
2. Puede ver eventos y tablero general de todas las promotoras.
3. Puede registrar una promotora desde el botón Registrar Promotora.
4. Puede seleccionar una promotora para agregar cuenta o administrador.
5. Crear/Modificar evento queda bloqueado para mantener la gestión descentralizada en las promotoras.

## Nota
El formulario visual específico de Registrar Promotora no estaba creado en el proyecto recibido. Para no alterar tus `.form`, el botón abre un formulario emergente funcional con nombre comercial, calle, número, colonia, ciudad y estado.

## Administrador interno - gestión de eventos
- El administrador interno puede ver eventos de todas las promotoras.
- Puede crear eventos: al entrar a Crear Evento se le solicita seleccionar la promotora.
- Puede modificar cualquier evento seleccionándolo en la tabla; las cuentas receptoras se cargan de la promotora propietaria del evento.
