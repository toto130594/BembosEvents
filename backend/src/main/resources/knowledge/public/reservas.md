# Bembos Events - reservas

Para preparar una reserva se consideran la sede, el salon, la fecha del evento, la hora de inicio, la hora de fin y la cantidad de invitados.

El paquete, la tematica y los servicios adicionales pueden formar parte de la reserva. Los servicios adicionales incluyen una cantidad seleccionada.

Antes de crear una reserva real, el backend valida la sede, el salon, la capacidad, la disponibilidad y los elementos activos del catalogo.

Los precios utilizados por una reserva se obtienen del backend. La creacion final de la reserva vuelve a ejecutar las reglas de negocio y puede rechazar datos que ya no sean validos.