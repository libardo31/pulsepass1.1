# PulsePass
## 1. Descripción
API REST académica para gestionar lugares (venues), eventos, artistas, usuarios, perfiles y entradas. El proyecto reúne las capas de persistencia, servicios y controladores de los tres talleres.
## 2. Tecnologías

- Java 21 y Spring Boot 4.1.1.
- Maven, Spring MVC, Spring Data JPA e Hibernate.
- PostgreSQL y Flyway.
- MapStruct para convertir entidades en DTOs.
- JUnit 5, Mockito, MockMvc y Testcontainers.
## 3. Modelo de datos

- `Venue`: código, nombre, ciudad, dirección, capacidad y estado activo.
- `Event`: código, nombre, descripción, categoría, estado, fecha, edad mínima y venue.
- `Artist`: nombre artístico, país, género y estado activo.
- `User`: username, email y estado activo.
- `UserProfile`: nombres, teléfono, ciudad y fecha de nacimiento.
- `Ticket`: código, tipo, precio, estado, fecha de compra, usuario y evento.
## 4. Relaciones

- Un venue puede albergar muchos eventos; cada evento tiene un venue.
- Eventos y artistas se relacionan muchos a muchos mediante `event_artists`.
- Cada usuario tiene como máximo un perfil.
- Un usuario puede tener muchas entradas; cada entrada pertenece a un usuario y a un evento.
- PostgreSQL protege códigos únicos, referencias, capacidad positiva, edad mínima y precio no negativo.
## 5. Ejecutar la base de datos

Requisitos: Docker Desktop con WSL 2 habilitado (Windows) o Docker Engine (Linux).

Desde la carpeta del proyecto:

```bash
docker compose up -d
```

Esto crea PostgreSQL en `localhost:5432`, base `pulsepass`, usuario `pulsepass` y contraseña `pulsepass`. El venue de referencia `VEN-SMR-01` y cinco artistas se cargan con Flyway. La capacidad del venue de referencia es 3 para poder comprobar el caso `SOLD_OUT` del taller. El PRD de persistencia presenta capacidad 5000, mientras que los PRD de servicios y controladores usan 3 para la aceptación; se conserva el valor 3 de estos dos últimos talleres. También se cargan Andrea y Carlos (activos y adultos), Laura (activa y menor de edad) y Miguel (inactivo). Sus correos de prueba son `andrea@email.com`, `carlos@email.com`, `laura@email.com` y `miguel@email.com`.

Para detener la base sin borrar datos:

```bash
docker compose stop
```

Para detenerla y borrar también los datos persistidos:

```bash
docker compose down -v
```
## 6. Configuración

Los valores por defecto están en `src/main/resources/application.properties`. Se pueden cambiar con las variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `SERVER_PORT`. Hibernate utiliza `ddl-auto=validate`; Flyway es el responsable de crear y evolucionar las tablas.
## 7. Ejecutar la API

Con el wrapper incluido (descarga Maven si no está instalado):

```bash
# Windows PowerShell
.\mvnw.cmd spring-boot:run

# Linux / WSL
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`. También se puede importar el proyecto como proyecto Maven en IntelliJ IDEA o Eclipse y ejecutar `PulsePassApplication`.
## 8. Ejecutar las pruebas

```bash
# Windows PowerShell
.\mvnw.cmd clean test

# Linux / WSL
./mvnw clean test
```

Las pruebas de controladores usan `@WebMvcTest`, MockMvc y servicios simulados con Mockito. Las pruebas de servicios comprueban reglas de negocio sin levantar Spring ni PostgreSQL. `PulsePassPersistenceTest` usa PostgreSQL real mediante Testcontainers y se omite automáticamente si Docker no está disponible.
## 9. Flyway

Flyway aplica las migraciones versionadas en orden y guarda el historial en `flyway_schema_history`:

- `V1__create_schema.sql`: tablas, claves, restricciones e índices.
- `V2__insert_initial_artists.sql`: catálogo inicial de artistas.
- `V3__add_streaming_url_to_event.sql`: agrega `streaming_url` como columna opcional.
- `V4__insert_reference_venue.sql`: agrega el venue de referencia del escenario académico.
- `V5__insert_reference_users.sql`: agrega los cuatro usuarios de prueba y sus perfiles.

No se deben modificar migraciones ya ejecutadas en una base compartida; los cambios nuevos se agregan en una nueva migración.
## 10. Testcontainers

Testcontainers levanta un contenedor PostgreSQL temporal durante las pruebas de integración. Así se prueban migraciones, restricciones y consultas contra PostgreSQL, no contra una base H2 que podría comportarse de forma distinta. Se requiere Docker para ejecutar esta prueba.
## 11. Consultas implementadas

**Query Methods:** búsqueda de venue por código; venues y artistas activos ordenados; evento por código; eventos por estado/fecha y venue; artista por nombre artístico; usuario por email ignorando mayúsculas y por username; tickets por usuario, evento y estado; conteo de entradas pagadas.

**JPQL:** eventos por artista; eventos por ciudad y artista; eventos recomendados por fecha, ciudad y texto del artista; conteo de tickets pagados; tickets asociados a eventos futuros. Las consultas usan nombres de entidades y relaciones, no SQL nativo.
## 12. Endpoints

| Método | Ruta | Función |
|---|---|---|
| GET | `/api/venues/{code}` | Consultar venue |
| GET | `/api/venues/active` | Listar venues activos |
| POST | `/api/events` | Crear evento (201) |
| GET | `/api/events/{eventCode}` | Consultar evento |
| GET | `/api/events/published` | Listar eventos publicados |
| PATCH | `/api/events/{eventCode}/publish` | Publicar evento |
| POST | `/api/events/{eventCode}/artists/{artistId}` | Asociar artista |
| GET | `/api/events/by-artist?stageName=Solar%20Beat` | Eventos por artista |
| GET | `/api/artists/{id}` | Consultar artista por ID |
| GET | `/api/artists/by-stage-name?stageName=Solar%20Beat` | Consultar por nombre artístico |
| GET | `/api/artists/active` | Listar artistas activos |
| POST | `/api/users` | Registrar usuario y perfil (201) |
| GET | `/api/users/by-email?email=...` | Consultar usuario por email |
| GET | `/api/users/by-username?username=...` | Consultar usuario por username |
| POST | `/api/tickets` | Comprar entrada (201) |
| GET | `/api/tickets/{ticketCode}` | Consultar entrada |
| GET | `/api/tickets/by-user?email=...` | Entradas de un usuario |
| GET | `/api/events/{eventCode}/tickets/paid` | Entradas pagadas de un evento |
| PATCH | `/api/tickets/{ticketCode}/cancel` | Cancelar entrada pagada |
| PATCH | `/api/tickets/{ticketCode}/use` | Marcar entrada como usada |
## 13. Reglas de negocio principales

- Un evento nuevo inicia en `DRAFT`; solo un evento `DRAFT`, futuro y con venue activo puede publicarse.
- No se puede registrar un evento con código repetido ni asociar dos veces el mismo artista.
- Usuarios nuevos quedan activos; username y email deben ser únicos. El perfil se registra en la misma transacción.
- Para comprar, el usuario debe existir y estar activo; el evento debe estar publicado, ser futuro y cumplir la edad mínima en la fecha del evento.
- Una compra válida genera una entrada `PAID`. Si se alcanza la capacidad del venue, el evento pasa a `SOLD_OUT`.
- Los precios están centralizados en `TicketServiceImpl`: GENERAL 120000, STUDENT 90000, VIP 250000 y BACKSTAGE 400000. No se acepta el precio desde el cliente.
- Solo se pueden cancelar entradas `PAID` antes del evento y solo entradas `PAID` pueden marcarse como `USED`.
## 14. Respuestas de error

Los errores utilizan `ErrorResponse` con `timestamp`, `status`, `error`, `message` y `details`. Los códigos principales son 400 (validación o JSON inválido), 404 (recurso inexistente), 409 (duplicado o regla de negocio) y 500 (error inesperado). Las entidades JPA no se exponen directamente por HTTP.
