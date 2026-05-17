# Recuperación de Contraseña - Cambios Implementados

## Archivos nuevos

| Archivo | Descripción |
|---------|-------------|
| `entity/PasswordReset.java` | Entidad JPA para la tabla `password_resets` |
| `repository/PasswordResetRepository.java` | Repositorio con queries para buscar resets activos por userId y por token |
| `dto/ForgotPasswordRequest.java` | DTO: `{ email }` |
| `dto/VerifyResetCodeRequest.java` | DTO: `{ email, code }` |
| `dto/ResetPasswordRequest.java` | DTO: `{ token, newPassword }` (min 8 chars) |
| `service/PasswordResetService.java` | Lógica de negocio: generar código 6 dígitos, validar, resetear contraseña |
| `service/EmailService.java` | Envío de email HTML con código + link directo (async) |
| `db/migration/V2__create_password_resets_table.sql` | Script SQL para crear la tabla en PostgreSQL |

## Archivos modificados

| Archivo | Cambio |
|---------|--------|
| `pom.xml` | Agregado `spring-boot-starter-mail` |
| `DesigeoAuthServiceApplication.java` | Agregado `@EnableAsync` |
| `controller/AuthController.java` | 3 endpoints nuevos: forgot-password, verify-reset-code, reset-password |
| `config/SecurityConfig.java` | Nuevos endpoints en `permitAll()` |
| `config/GlobalExceptionHandler.java` | Handler para `IllegalArgumentException` |
| `application.properties` | Config SMTP + `app.mail.from` + `app.frontend.base-url` |

## Endpoints

| Método | Ruta | Body | Respuesta |
|--------|------|------|-----------|
| POST | `/api/auth/forgot-password` | `{ "email": "..." }` | `{ "ok": true }` (siempre 200, no revela si email existe) |
| POST | `/api/auth/verify-reset-code` | `{ "email": "...", "code": "123456" }` | `{ "ok": true, "token": "uuid" }` |
| POST | `/api/auth/reset-password` | `{ "token": "uuid", "newPassword": "..." }` | `{ "ok": true }` |

## Seguridad

- Código de 6 dígitos almacenado como hash BCrypt
- Token UUID único por solicitud
- Expiración: 15 minutos
- Máximo 5 intentos de verificación de código
- Al resetear: se limpia `failedLoginCount` y `lockedUntil` del usuario
- No se revela si el email existe en forgot-password

## Variables de entorno requeridas

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password
MAIL_FROM=noreply@desigeo.com
FRONTEND_BASE_URL=https://tu-frontend.com
```

## SQL (ejecutar manualmente en PostgreSQL)

El script está en `src/main/resources/db/migration/V2__create_password_resets_table.sql`.
Como `ddl-auto=validate`, debes ejecutar el SQL en tu base de datos antes de usar los endpoints.
