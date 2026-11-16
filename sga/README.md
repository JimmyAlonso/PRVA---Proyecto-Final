# SGA — JiyJu Glamping (backend, Entrega 2)

Spring Boot 4 · Java 25 · Gradle · H2 · Spring Security + JWT · MapStruct · springdoc.
Arquitectura hexagonal: `domain` (Java puro), `application` (casos de uso con `@Service` y
`@Transactional`, DTOs) e `infrastructure` (REST, persistencia JPA, seguridad, configuración).

## Ejecutar

```bash
# Opcional: clave del JWT (mínimo 32 caracteres). Nunca se versiona.
export SGA_JWT_SECRETO="una-clave-larga-de-al-menos-32-caracteres"
./gradlew bootRun
```

Sin `SGA_JWT_SECRETO` la aplicación genera una clave aleatoria al arrancar: sirve para
desarrollo, pero los tokens dejan de valer al reiniciar.

- Swagger UI: http://localhost:8080/swagger-ui.html (botón **Authorize**: pegar el `token` de `POST /api/v1/auth/login`).
- Consola H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:sgadb`, usuario `sa`, sin contraseña).
- Base en archivo (opcional): `export SGA_DB_URL=jdbc:h2:file:./data/sgadb`.

Al arrancar se cargan solos los datos de prueba (12.3): los 5 domos con tarifas, temporadas,
política, reservas en todos los estados y por los tres canales, un bloqueo vigente, un domo
fuera de servicio y un usuario por rol.

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@jiyju.co | Admin2026 |
| Recepcionista | recepcion@jiyju.co | Recepcion2026 |
| Personal de servicio | servicio@jiyju.co | Servicio2026 |
| Huésped | valentina.rios@correo.com | Huesped2026 |
| Huésped | andres.cardona@correo.com | Huesped2026 |

## Pruebas

```bash
./gradlew test
```

## Documentación

- `docs/api/openapi.yaml`: contrato de la API (coincide con los endpoints implementados).
- `docs/api/sga.postman_collection.json`: colección de pruebas de Postman. Se importa en Postman
  (Import → archivo) y se ejecuta completa con **Run collection** contra la aplicación recién
  arrancada: flujo completo de una reserva, un 409 por cada regla, 401 y 403. Cada petición
  verifica su código de respuesta y guarda solos los tokens, las fechas y los códigos.
- La carpeta "7. Canal externo" de la colección es el simulador del canal externo (10.3.1):
  reserva aceptada, mensaje repetido que no duplica, conflicto registrado y cancelación.
- `docs/trazabilidad.md`: matriz de trazabilidad con el endpoint que expone cada regla.

## Despliegue (Guía 22)

El backend se despliega en **Render** como contenedor Docker (`Dockerfile` de esta carpeta) con el
perfil `prod` (`application-prod.properties`). En producción:

- Swagger UI y la consola de H2 quedan apagados.
- Solo se carga el catálogo de la Ficha A (domos, temporadas y política); no se crean los usuarios
  de demostración ni las reservas de ejemplo.
- La única cuenta es un administrador cuyos datos llegan por variables de entorno.
- `GET /actuator/health` es público para el health check de Render.

### Servicio en Render

| Campo | Valor |
|---|---|
| Language | `Docker` |
| Branch | `master` |
| Root Directory | `sga` |
| Instance Type | `Free` |
| Health Check Path | `/actuator/health` |

### Variables de entorno

| Variable | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SGA_JWT_SECRETO` | Salida de `openssl rand -hex 32` (obligatoria; sin ella no arranca) |
| `SGA_CORS_ALLOWED_ORIGINS` | URL exacta del frontend en Vercel, sin `/` final (varias separadas por coma) |
| `SGA_USUARIOS_INICIALES_HABILITADOS` | `true` |
| `SGA_ADMIN_CORREO` | Correo del administrador |
| `SGA_ADMIN_CONTRASENA` | Contraseña privada (no se publica en el repositorio) |
| `SGA_ADMIN_TELEFONO` | Teléfono válido |
| `SGA_ADMIN_DOCUMENTO` | Documento del administrador |
| `SGA_ADMIN_FECHA_NACIMIENTO` | `AAAA-MM-DD` |
| `SGA_CANAL_BOOKING_CLAVE` | Opcional: credencial del canal externo. Sin ella el canal queda cerrado. |

`PORT` no se define: lo pone Render.

### Probar el perfil de producción en local

```bash
SPRING_PROFILES_ACTIVE=prod \
SGA_JWT_SECRETO="$(openssl rand -hex 32)" \
SGA_CORS_ALLOWED_ORIGINS=http://localhost:5173 \
SGA_ADMIN_CORREO=admin.prueba@jiyju.co \
SGA_ADMIN_CONTRASENA=ClavePrueba2026 \
SGA_ADMIN_TELEFONO=3000000000 \
SGA_ADMIN_DOCUMENTO=123456789 \
SGA_ADMIN_FECHA_NACIMIENTO=1990-01-01 \
./gradlew bootRun
```

| Prueba | Resultado esperado |
|---|---|
| `curl http://localhost:8080/actuator/health` | `{"status":"UP"}` |
| `curl http://localhost:8080/api/v1/domos` | Catálogo de los 5 domos |
| Abrir `http://localhost:8080/swagger-ui.html` | `401`: Swagger apagado |
| `POST /api/v1/auth/login` con `admin.prueba@jiyju.co` | `200` con el token |
| Repetir sin `SGA_ADMIN_TELEFONO` | No arranca: `Complete los datos de la cuenta inicial del SGA` |

Preflight de CORS:

```bash
curl -i -X OPTIONS http://localhost:8080/api/v1/auth/login \
  -H "Origin: http://localhost:5173" -H "Access-Control-Request-Method: POST"
# 200 + Access-Control-Allow-Origin: http://localhost:5173

curl -i -X OPTIONS http://localhost:8080/api/v1/auth/login \
  -H "Origin: https://sitio-ajeno.com" -H "Access-Control-Request-Method: POST"
# 403 Invalid CORS request
```

> Plan gratuito de Render: el servicio se duerme tras 15 minutos sin tráfico (la primera petición
> tarda cerca de un minuto) y la base H2 en memoria vuelve al catálogo inicial en cada arranque.
