# E-Commerce API

API REST para una tienda online que hice para practicar Spring Boot. Maneja usuarios,
productos, categorias, ordenes y direcciones. La autenticacion es con JWT y hay dos roles:
ADMIN y CUSTOMER.

## Tecnologias que use

- Java 21
- Spring Boot 3.3
- Spring Security + JWT (libreria jjwt)
- Spring Data JPA
- MySQL 8
- Maven
- Lombok
- MapStruct
- Swagger (springdoc openapi)
- JUnit 5 y Mockito para los tests
- Docker y Docker Compose

## Como correrlo

### Con Docker (lo mas facil)

Solo necesitas tener Docker instalado y correr:

```
docker compose up --build
```

Eso levanta la base de datos MySQL y la app. La API queda en http://localhost:8080

### En local sin Docker

Necesitas tener MySQL corriendo y una base de datos llamada `ecommerce_db`. Despues:

```
mvn spring-boot:run
```

Las credenciales de la base se leen de variables de entorno pero tienen valores por defecto
(root / root), asi que si tu MySQL local usa eso deberia funcionar directo.

## Swagger

Una vez que la app esta corriendo puedes ver toda la documentacion en:

```
http://localhost:8080/swagger-ui.html
```

Ahi tambien puedes probar los endpoints. Para los que necesitan login primero haces el login,
copias el token y lo pegas en el boton de Authorize (arriba a la derecha).

## Usuarios de prueba

Cuando arranca la app se cargan estos usuarios automaticamente:

- Admin: admin@store.com / admin123
- Cliente: user@store.com / user123

## Endpoints principales

Auth:
- POST /api/auth/register
- POST /api/auth/login

Productos:
- GET /api/products (publico, paginado, se puede filtrar con ?category=)
- GET /api/products/{id}
- POST /api/products (solo admin)
- PUT /api/products/{id} (solo admin)
- DELETE /api/products/{id} (solo admin)

Categorias:
- GET /api/categories
- POST /api/categories (solo admin)

Ordenes:
- POST /api/orders (crea la orden y descuenta stock)
- GET /api/orders (lista las tuyas, el admin ve todas)
- GET /api/orders/{id}
- DELETE /api/orders/{id} (cancela y devuelve el stock)
- PATCH /api/orders/{id}/status (solo admin, cambia el estado)

Direcciones:
- GET /api/addresses
- POST /api/addresses
- DELETE /api/addresses/{id}

## Estados de una orden

Una orden empieza en PENDING. Los cambios de estado validos son:

- PENDING -> CONFIRMED o CANCELLED
- CONFIRMED -> SHIPPED o CANCELLED
- SHIPPED -> DELIVERED
- DELIVERED -> REFUNDED

Si intentas un cambio que no esta permitido la API devuelve error 400.

## Tests

Para correr los tests:

```
mvn test
```

Hay tests para los servicios de ordenes, productos y autenticacion.

## Notas

Use ddl-auto en create-drop, asi que cada vez que reinicias la app se borra y vuelve a crear
la base con los datos de prueba. Para algo real habria que cambiarlo, pero para probar va bien.
