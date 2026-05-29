# 🛒 E-Commerce API

API REST para un sistema de comercio electrónico, desarrollada con Spring Boot. Implementa las operaciones esenciales de una tienda online: gestión de productos, categorías, órdenes de compra y direcciones de envío, con autenticación basada en JWT.

## ¿Qué hace?

Permite registrar usuarios, explorar productos por categoría, crear órdenes de compra con control automático de inventario, y gestionar el ciclo de vida de cada orden desde que se crea hasta que se entrega. Incluye dos roles de usuario: administrador y cliente, cada uno con distintos niveles de acceso.

El proyecto se construyó por capas: modelo de datos, repositorios, seguridad, lógica de negocio y controladores REST, lo que facilita su mantenimiento y escalabilidad.

## Seguridad

La autenticación se maneja con JSON Web Tokens. Al registrarse o iniciar sesión, el servidor devuelve un token que debe incluirse en las peticiones protegidas. Las contraseñas se almacenan encriptadas con BCrypt. Los permisos están diferenciados por rol: los clientes acceden al catálogo y gestionan sus propias órdenes, mientras que los administradores pueden crear y modificar productos y categorías.

## Tecnologías

- Java 21 con Spring Boot 3.3
- MySQL 8
- Maven
- Lombok y MapStruct
- Swagger
- JUnit 5 y Mockito
- Docker

## Estructura

El proyecto sigue el patrón controller → service → repository. Las entidades se mapean con JPA, los servicios contienen la lógica de negocio y las validaciones, y los controladores exponen los endpoints bajo el prefijo /api. La comunicación entre capas usa DTOs para mantener separadas las representaciones internas de las externas.
