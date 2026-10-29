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
