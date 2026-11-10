# Sistema de Gestion de Reservas con Tickets - Cloud Native (Semana 3)
Asignatura: Desarrollo Cloud Native (CDY2204) - Duoc UC

## 1. Descripcion del Proyecto
Microservicio desarrollado en Java 21 con Spring Boot 3 para la gestion de tickets de eventos.
Cumple con los siguientes requerimientos de arquitectura cloud:
- Almacenamiento temporal en Amazon EFS (/app/efs).
- Subida automatica a Amazon S3 organizada por evento y usuario (/evento123/usuarioX/ticket001.pdf).
- Endpoints REST para generar, subir, descargar, actualizar, eliminar tickets y consultar historial y estadisticas.
- Despliegue continuo (CI/CD) con GitHub Actions hacia una instancia AWS EC2 corriendo en Docker.

## 2. Endpoints de la API REST
Base URL: http://localhost:8080/api/tickets (o IP publica de EC2:8080/api/tickets)

1. POST /api/tickets/generate
   Genera el ticket en PDF, lo guarda en EFS y lo sube automatico a S3.
   Body JSON:
   {
     "eventId": "evento123",
     "eventName": "Conferencia Cloud",
     "userId": "usuarioX",
     "userName": "Carlos Perez",
     "userEmail": "carlos@duocuc.cl",
     "seatNumber": "A-15",
     "price": 35000.0
   }

2. GET /api/tickets/{ticketId}/download
   Descarga el archivo PDF binario del ticket desde S3.

3. GET /api/tickets/download?eventId=evento123&userId=usuarioX
   Descarga el ticket buscando por identificadores de evento y usuario.

4. POST /api/tickets/{ticketId}/upload-s3
   Sube o resincroniza manualmente el archivo a S3.

5. PUT /api/tickets/{ticketId}
   Modifica datos del ticket (asiento, precio, usuario) y actualiza el archivo en S3 y EFS.
   Body JSON:
   {
     "userName": "Carlos Perez Actualizado",
     "seatNumber": "VIP-01",
     "price": 50000.0,
     "status": "CONFIRMED"
   }

6. DELETE /api/tickets/{ticketId}
   Elimina el ticket de S3, EFS y catalogo.

7. GET /api/tickets/history?eventId=evento123
   Consulta historial de tickets con filtros (por evento, usuario o estado).

8. GET /api/tickets/statistics/{eventId}
   Reporta estadisticas de ventas, reservas y ocupacion por evento.

9. GET /api/tickets/health
   Diagnostico de salud del servicio, ruta EFS y estado de S3.

## 3. Pruebas con Postman
Importar la coleccion Ticket_Reservation_System_Postman_Collection.json en Postman.
Contiene todas las peticiones listas para ejecutar.

## 4. Despliegue CI/CD
El archivo .github/workflows/deploy.yml ejecuta:
1. Compilacion y ejecucion de pruebas con Maven.
2. Construccion y publicacion de imagen Docker en Docker Hub.
3. Despliegue automatico en EC2 montando el volumen EFS:
   docker run -d --name my-app -v /mnt/efs:/app/efs -p 8080:8080 ${{ secrets.DOCKERHUB_USERNAME }}/my-app:latest
