# DESIGEO Backend — Sistema de Denuncia Ciudadana Geolocalizada

Sistema de microservicios para la gestión de denuncias ciudadanas a nivel municipal en Chile.

---

## 🏗️ Arquitectura

| Microservicio | Puerto | Descripción |
|---|---|---|
| api-gateway | 8080 | Punto de entrada único, enrutamiento y seguridad |
| desigeo-auth-service | 8081 | Autenticación y generación de JWT |
| desigeo-report-service | 8082 | CRUD de reportes ciudadanos con geolocalización |
| gestion-de-usuarios | 8087 | CRUD de usuarios |

---

## ⚙️ Requisitos

- Java 17
- Maven (o usar `./mvnw`)
- Acceso a Supabase (PostgreSQL)

---

## 🚀 Cómo levantar el proyecto

### 1. Clonar el repositorio

```bash
git clone https://github.com/mescobarg1120/backend-desigeo.git
cd backend-desigeo
```

### 2. Configurar variables de entorno

Cada microservicio tiene un `.env.example`. Copia y completa con las credenciales reales:

```bash
cp desigeo-auth-service/.env.example desigeo-auth-service/.env
cp gestion-de-usuarios/.env.example gestion-de-usuarios/.env
cp api-gateway/.env.example api-gateway/.env
```

### 3. Levantar todos los servicios

```bash
chmod +x start-all.sh
./start-all.sh
```

O manualmente por servicio:

```bash ejemplo
cd /home/mario-escobar/Documentos/Github/backend-desigeo/desigeo-auth-service
export $(cat .env | xargs) && ./mvnw spring-boot:run

cd /home/mario-escobar/Documentos/Github/backend-desigeo/gestion-de-usuarios
export $(cat .env | xargs) && ./mvnw spring-boot:run

cd /home/mario-escobar/Documentos/Github/backend-desigeo/api-gateway
export $(cat .env | xargs) && ./mvnw spring-boot:run
```

En Windows, configura primero las variables de entorno de cada servicio en la misma terminal antes de arrancarlo. Ejemplo en PowerShell para `gestion-de-usuarios`:

```powershell
Set-Location C:\workspace\Estudio\backend-desigeo\backend-desigeo\gestion-de-usuarios
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:6543/postgres"
$env:SPRING_DATASOURCE_USERNAME = "postgres.xwkcinfiicrquhwtziqg"
$env:SPRING_DATASOURCE_PASSWORD = "<tu_password_de_supabase>"
./mvnw spring-boot:run
```

Si esas variables no están presentes, Spring usa el valor por defecto `jdbc:postgresql://localhost:5432/desigeo_db` y el servicio falla si no tienes PostgreSQL local levantado.


### 4. Detener todos los servicios

```bash
kill $(lsof -ti :8080,8081,8087)
```

---

## 📡 API Endpoints

Todas las rutas pasan por el **API Gateway** en `http://localhost:8080`.

### Registro de usuario
```
POST /api/users
Content-Type: application/json
```
```json
{
  "email": "usuario@desigeo.cl",
  "password": "12345678",
  "fullName": "Nombre Apellido",
  "phone": "+56987654321",
  "rut": "12345678K"
}
```
> El rol se asigna como `CITIZEN` por defecto. Solo `ADMIN_MASTER` puede cambiar roles.
> El RUT debe enviarse sin puntos ni guión: `12345678K`

---

### Login
```
POST /api/auth/login
Content-Type: application/json
```
```json
{
  "email": "usuario@desigeo.cl",
  "password": "12345678"
}
```
**Respuesta:**
```json
{
  "token": "eyJhbGci...",
  "tokenType": "Bearer",
  "user": {
    "userId": "uuid",
    "email": "usuario@desigeo.cl",
    "fullName": "Nombre Apellido",
    "roleName": "CITIZEN",
    "active": true
  }
}
```

---

### Ver usuario

GET /api/users/{userId}
Authorization: Bearer {token}




---

### Actualizar usuario

PUT /api/users/{userId}
Authorization: Bearer {token}
Content-Type: application/json

```json
{
  "fullName": "Nombre Actualizado"
}
```

---

### Eliminar usuario

DELETE /api/users/{userId}
Authorization: Bearer {token}

---

### Crear reporte
```
POST /api/reports
Authorization: Bearer {token}
Content-Type: application/json
```
```json
{
  "description": "Descripción del problema (mín. 10 caracteres)",
  "latitude": -33.4569,
  "longitude": -70.6483,
  "address": "Calle Principal 123, Santiago"
}
```
**Respuesta:**
```json
{
  "reportId": "uuid",
  "status": "PENDING",
  "createdAt": "2026-05-13T14:44:48.767Z"
}
```

---

### Ver reporte

```
GET /api/reports/{reportId}
Authorization: Bearer {token}
```

---

### Listar reportes

```
GET /api/reports
Authorization: Bearer {token}
```

Parámetros opcionales: `status`, `category`, `priority`, `userId`, `startDate`, `endDate`, `lat`, `lng`, `radius`, `page`, `size`

---

### Cambiar estado de reporte

```
PATCH /api/reports/{reportId}/status
Authorization: Bearer {token}
Content-Type: application/json
```
```json
{
  "status": "IN_PROGRESS",
  "comment": "Comentario opcional"
}
```
> Estados válidos: `PENDING`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `REOPENED`

---

### Reabrir reporte

```
POST /api/reports/{reportId}/reopen
Authorization: Bearer {token}
Content-Type: application/json
```
```json
{
  "reason": "Razón de la reapertura"
}
```

---

### Reportes por usuario

```
GET /api/reports/user/{userId}
Authorization: Bearer {token}
```

---

## 👥 Roles del sistema

| Rol | Descripción |
|---|---|
| `CITIZEN` | Ciudadano — puede crear y ver reportes |
| `AGENT` | Agente municipal — gestiona reportes |
| `ADMIN_COMUNAL` | Administrador comunal |
| `ADMIN_MASTER` | Administrador maestro — gestión total |

---

## 🧪 Colección Postman

Importa `postman_collection.json` en Postman para probar todos los endpoints disponibles.

---

## 👨‍💻 Equipo

| Nombre | Rol |
|---|---|
| Mario Escobar | Tech Lead / Backend |
| Areliz Isla | Product Owner / Frontend / QA |
| Marcos Hidalgo | Scrum Master / DBA |

---

## 📚 Tecnologías

- Java 17 + Spring Boot 3
- Spring Security + JWT
- Spring Data JPA + PostgreSQL (Supabase)
- Firebase Admin SDK + Firestore (reportes)
- Supabase Storage (imágenes de reportes)
- Spring Cloud Gateway









#####  Actaulizacion para perfil super_admin en analitics

# gestion-de-usuarios

Microservicio de **DESIGEO** encargado de la gestión de usuarios y comunas del sistema.  
Forma parte del backend de microservicios construido con **Java 17 + Spring Boot 3**.

---

## Stack

- Java 17 + Spring Boot 3
- Spring Data JPA
- Supabase PostgreSQL (via Session Pooler)
- JWT (validado por el API Gateway, no por este servicio)

---

## Puerto

```
8087
```

Todas las peticiones llegan a través del **API Gateway (puerto 8080)**, que inyecta los headers de autenticación antes de redirigir al servicio.

---

## Variables de entorno

Crear un archivo `.env` en la raíz del servicio (no se versiona):

```env
DB_URL=jdbc:postgresql://<host>:<puerto>/<db>
DB_USERNAME=postgres
DB_PASSWORD=<password>
JWT_SECRET=<secret>
```

Para levantar el servicio:

```bash
export $(grep -v '^#' .env | xargs) && ./mvnw spring-boot:run
```

---

## Estructura del proyecto

```
src/main/java/com/backend_desigeo/gestion_de_usuarios/
├── config/
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── UserController.java
│   └── ComunaController.java        ← nuevo
├── dto/
│   ├── UserDto.java
│   ├── UserCreateDto.java
│   ├── UserUpdateDto.java
│   ├── ComunaDto.java               ← nuevo
│   └── ComunaCreateDto.java         ← nuevo
├── entity/
│   ├── User.java
│   ├── Role.java
│   ├── RoleName.java
│   └── ComunaEntity.java            ← nuevo
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── ComunaRepository.java        ← nuevo
├── security/
│   └── GatewayAuthFilter.java
└── service/
    ├── UserService.java
    └── ComunaService.java           ← nuevo
```

---

## Endpoints de Usuarios

| Método | Ruta | Rol requerido | Descripción |
|--------|------|---------------|-------------|
| `POST` | `/api/users` | Público | Registro de ciudadano |
| `POST` | `/api/users` | `SUPER_ADMIN` | Crear usuario con rol específico |
| `GET` | `/api/users` | `SUPER_ADMIN` | Listar todos los usuarios |
| `GET` | `/api/users/{id}` | Autenticado | Ver detalle de usuario |
| `PUT` | `/api/users/{id}` | `SUPER_ADMIN` / `ADMIN_MUNICIPAL` | Editar usuario |
| `PATCH` | `/api/users/{id}/role` | `SUPER_ADMIN` / `ADMIN_MUNICIPAL` | Cambiar rol |
| `PATCH` | `/api/users/{id}/toggle` | `SUPER_ADMIN` / `ADMIN_MUNICIPAL` | Activar/desactivar |
| `DELETE` | `/api/users/{id}` | `SUPER_ADMIN` | Eliminar usuario |

---

## Endpoints de Comunas

Agregados en la sesión de desarrollo del **01-06-2026**.

| Método | Ruta | Rol requerido | Descripción |
|--------|------|---------------|-------------|
| `GET` | `/api/comunas` | Autenticado | Listar todas las comunas |
| `GET` | `/api/comunas?isActive=true` | Autenticado | Filtrar por estado |
| `GET` | `/api/comunas/{id}` | Autenticado | Detalle de una comuna |
| `POST` | `/api/comunas` | `SUPER_ADMIN` | Crear comuna |
| `PUT` | `/api/comunas/{id}` | `SUPER_ADMIN` | Editar comuna |
| `PATCH` | `/api/comunas/{id}/toggle` | `SUPER_ADMIN` | Activar/desactivar |
| `DELETE` | `/api/comunas/{id}` | `SUPER_ADMIN` | Eliminar comuna |

### Notas importantes

- El `GET` es accesible para todos los roles autenticados porque el frontend lo necesita para poblar selectores de comuna al crear/editar usuarios.
- El rol se valida mediante el header `X-User-Role` inyectado por el API Gateway.
- **No se puede eliminar una comuna que tenga agentes asignados** — el servicio lanza un error con el conteo de agentes afectados.
- La respuesta incluye `agentCount`: número de usuarios con ese `comunaId` asignado.

### Ejemplo de respuesta `GET /api/comunas`

```json
[
  {
    "comunaId": 1,
    "nombre": "Providencia",
    "region": "Metropolitana",
    "codigoIne": "13120",
    "isActive": true,
    "createdAt": "2026-01-09T12:00:00Z",
    "agentCount": 8
  }
]
```

---

## Autenticación

Este servicio **no valida JWT directamente**. El API Gateway extrae los claims del token y los reenvía como headers HTTP:

| Header | Contenido |
|--------|-----------|
| `X-User-Id` | UUID del usuario autenticado |
| `X-User-Role` | Nombre del rol (`CITIZEN`, `MUNICIPAL_OFFICER`, etc.) |
| `X-User-Email` | Email del usuario |

El `GatewayAuthFilter` lee estos headers y construye el contexto de seguridad de Spring.

---

## Roles del sistema

| Rol | Descripción |
|-----|-------------|
| `CITIZEN` | Ciudadano — solo puede ver y crear sus propios reportes |
| `MUNICIPAL_OFFICER` | Agente municipal — gestiona reportes de su comuna |
| `ADMIN_MUNICIPAL` | Administrador de municipio — gestiona usuarios y configuración de su comuna |
| `SUPER_ADMIN` | Administrador global — acceso total al sistema |

---

## Pendiente

- Tabla `audit_logs` + triggers PostgreSQL para auditoría de acciones críticas (cambio de rol, activación/desactivación, login fallido)
- Endpoint de auditoría para consumo del SUPER_ADMIN
