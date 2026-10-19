# Integración de seguridad en PulsePass (Spring Boot 4.1.1 / Java 21)

Este paquete contiene archivos nuevos, no una copia completa del proyecto. Integra los archivos en la raíz de tu proyecto en la rama `feature/security-jwt`.

## 1. Dependencias que debes añadir a `pom.xml`

Dentro de `<dependencies>` añade:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.13.0</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.13.0</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.13.0</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-test</artifactId>
  <scope>test</scope>
</dependency>
```

## 2. Variables de configuración

Añade al final de `src/main/resources/application.properties`:

```properties
pulsepass.security.jwt.secret-base64=${PULSEPASS_JWT_SECRET_BASE64}
pulsepass.security.jwt.issuer=${PULSEPASS_JWT_ISSUER:pulsepass-api}
pulsepass.security.jwt.expiration-seconds=${PULSEPASS_JWT_EXPIRATION_SECONDS:900}
```

Genera un secreto de desarrollo aleatorio de 32 bytes o más y guárdalo como Base64, nunca en Git. En PowerShell puedes ejecutar:

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:PULSEPASS_JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
```

Esa variable solo dura en esa sesión de PowerShell. Debes configurarla también en el entorno desde el que ejecutas la aplicación. No reutilices secretos reales en pruebas.

## 3. Ampliar `RegisterUserRequest`

Añade al final del record, después de `birthDate`, este componente (con la coma después de `birthDate`):

```java
@NotBlank(message = "Password is required")
@Size(min = 8, max = 72, message = "Password must contain between 8 and 72 characters")
String password
```

BCrypt trabaja con un límite efectivo de 72 bytes; para evitar sorpresas con caracteres multibyte, el proyecto debería validar también ese límite por bytes antes de producción.

## 4. Modificar `UserServiceImpl` (registro atómico)

Añade estos imports:

```java
import com.pulsepass.security.domain.UserCredential;
import com.pulsepass.security.repository.UserCredentialRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
```

Añade campos `private final UserCredentialRepository credentialRepository;`, `private final PasswordEncoder passwordEncoder;` y `private final JdbcTemplate jdbcTemplate;`. Actualiza el constructor para recibir y asignar esos tres argumentos además de `UserRepository` y `PulsePassMapper`.

En `register`, después de `userRepository.save(user)`, guarda la credencial y el rol dentro de la misma transacción:

```java
User savedUser = userRepository.save(user);
credentialRepository.save(new UserCredential(savedUser, passwordEncoder.encode(request.password())));
jdbcTemplate.update("insert into user_roles (user_id, role) values (?, 'USER')", savedUser.getId());
return mapper.toResponse(savedUser);
```

El método ya tiene `@Transactional`, por lo que cualquier fallo al guardar la credencial/rol revierte también la creación del usuario. No aceptes roles desde el DTO público.

**Actualiza las pruebas unitarias de `UserServiceImplTest`** para inyectar mocks de `UserCredentialRepository`, `PasswordEncoder` y `JdbcTemplate`, configurar el hash devuelto y verificar que se guarda el rol `USER`. No elimines las pruebas anteriores.

## 5. Seguridad de los endpoints y propiedad de recursos

Añade `@PreAuthorize` en los métodos de las implementaciones de servicio, no solo en los Controllers. Imports:

```java
import org.springframework.security.access.prepost.PreAuthorize;
```

En `EventServiceImpl`:
- `create(...)`, `publish(...)` y `addArtist(...)`: `@PreAuthorize("hasRole('ADMIN')")`.
- Para `findByCode(...)`, añade comprobación que permita solo eventos publicados a usuarios ordinarios; los borradores solo deben poder consultarse por ADMIN. Puedes implementar `AuthorizationService.canReadEvent(authentication, eventCode)` con `EventRepository` y usar `@PreAuthorize("@authorizationService.canReadEvent(authentication, #p0)")`. El parámetro `#p0` evita depender de metadatos del nombre del parámetro.

En `UserServiceImpl`:
- `findByEmail(String email)`: `@PreAuthorize("@authorizationService.isSelfOrAdmin(authentication, #p0)")`.
- `findByUsername(String username)`: `@PreAuthorize("@authorizationService.isSelfOrAdminByUsername(authentication, #p0)")`.
- `register(...)` queda público mediante la regla HTTP; no agregues un rol privilegiado.

En `TicketServiceImpl`:
- `purchase(PurchaseTicketRequest request)`: `@PreAuthorize("@authorizationService.isSelfOrAdmin(authentication, #p0.userEmail())")`.
- `findByUserEmail(String email)`: `@PreAuthorize("@authorizationService.isSelfOrAdmin(authentication, #p0)")`.
- `findByCode(String code)` y `cancel(String code)`: `@PreAuthorize("@authorizationService.ownsTicketOrIsAdmin(authentication, #p0)")`.
- `markAsUsed(String code)`: `@PreAuthorize("hasAnyRole('STAFF','ADMIN')")`.
- `findPaidTicketsByEvent(String eventCode)`: `@PreAuthorize("hasAnyRole('STAFF','ADMIN')")`.

**Importante:** la autorización de `purchase` se ejecuta antes del método y bloquea el correo ajeno antes de crear el ticket. Los roles `STAFF` y `ADMIN` deben provisionarse por un procedimiento administrativo controlado; nunca desde el registro público.

## 6. Regla de rutas y datos públicos

`SecurityConfig` deja públicas las consultas de venues/artistas, eventos publicados y búsqueda pública por artista, además de registro y login. Cualquier otra ruta requiere token. El método `findByCode` debe tener además la regla de publicación indicada arriba; autenticar no debe revelar un borrador.

La clase `AuthorizationService` incluida necesita este método adicional para la regla de evento:

```java
public boolean canReadEvent(Authentication authentication, String eventCode) {
    if (authentication != null && authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) return true;
    return events.findByEventCode(eventCode)
            .map(event -> event.getStatus() == com.pulsepass.domain.EventStatus.PUBLISHED)
            .orElse(false);
}
```

Para compilarlo, inyecta también `EventRepository` en `AuthorizationService`, decláralo como campo `private final EventRepository events;` y asígnalo en el constructor.

## 7. Migración y usuarios legados

Copia `V6__security_credentials_roles.sql` a `src/main/resources/db/migration`. No edites V1–V5. Los cuatro usuarios de demostración preexistentes no reciben contraseña ni rol automáticamente y no podrán iniciar sesión hasta que se activen de forma segura. No se crea una contraseña por defecto.

Para asignar un rol a un usuario que ya tenga credenciales, usa una operación administrativa controlada en tu base de datos. Por ejemplo, para un usuario previamente registrado:

```sql
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'tu_usuario'
ON CONFLICT DO NOTHING;
```

No ejecutes esto en producción sin control de acceso a la base de datos. `STAFF` y `ADMIN` no se asignan mediante HTTP público.

## 8. Crear rama y probar

Desde la raíz del repositorio:

```bash
git status
git switch -c feature/security-jwt
```

Integra los archivos y cambios anteriores en esa rama. Después:

```bash
mvn clean test
```

Arranca PostgreSQL como lo haces normalmente y ejecuta la aplicación con `PULSEPASS_JWT_SECRET_BASE64` definido. Prueba `POST /api/users` con `password` y luego `POST /api/auth/login` con `login` (username o email) y `password`. Envía el token en `Authorization: Bearer <token>`.

## Estado de verificación

El ZIP de adiciones contiene los nuevos componentes y la migración. No se ha podido ejecutar Maven en el entorno de preparación (no hay ejecutable `mvn` disponible), por lo que no debe considerarse verificado hasta aplicar los cambios, añadir las pruebas de seguridad y ejecutar `mvn clean test` en tu equipo. La matriz de 20 endpoints y los tests MockMvc/Testcontainers del PRD siguen siendo una parte necesaria para afirmar cumplimiento total.
