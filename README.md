# DESIGEO Backend — Sistema de Denuncia Ciudadana Geolocalizada

Sistema de microservicios para la gestión de denuncias ciudadanas a nivel municipal en Chile.

---

## 🏗️ Arquitectura

| Microservicio | Puerto | Descripción |
|---|---|---|
| api-gateway | 8080 | Punto de entrada único, enrutamiento y seguridad |
| desigeo-auth-service | 8081 | Autenticación y generación de JWT |
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
- Spring Cloud Gateway