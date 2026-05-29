# 🛒 E-Commerce API

Una api que hice para practicar Spring Boot. Es básicamente el backend de una tienda online. Nada del otro mundo pero le metí varias cosas que fuí aprendiendo.

## ¿Qué hace?

Lo típico de un ecommerce: te registras, ves productos por categoría, armas una orden y la app te descuenta el stock automáticamente. También maneja direcciones de envío, estados de las órdenes, y tiene roles para que no cualquiera pueda modificar productos.

Lo fui armando por capas: primero las entidades con las relaciones entre sí, después los repositorios, luego la parte de seguridad, los servicios con la lógica, y al final los controladores que exponen todo.

## Seguridad

Usé JWT con Spring Security. Básicamente cuando te registras o inicias sesión te devuelve un token, y con eso ya puedes usar los endpoints protegidos. Las contraseñas van encriptadas con BCrypt, no se guardan en texto plano. También separé los permisos: un cliente normal puede ver productos y crear órdenes, pero solo un admin puede crear productos nuevos o cambiar categorías.

## Lo que me gustó hacer

- El servicio de órdenes fue lo más entretenido: valida stock, calcula el total, y si cancelas te devuelve los productos al inventario. Tuve un bug tonto al principio donde no validaba que la orden viniera con productos... pero ya lo arreglé.
- Las excepciones personalizadas también quedaron bien. En vez de devolver errores genéricos, la api responde con mensajes claros en español.
- Probé MapStruct por primera vez, está bueno para no andar haciendo conversiones a mano entre entidades y DTOs.

## Tecnologías

- Java 21 con Spring Boot 3.3
- MySQL 8, aunque se puede cambiar facil
- Maven para las dependencias
- Lombok y MapStruct para no escribir tanto código repetitivo
- Swagger para documentar los endpoints
- JUnit y Mockito para las pruebas
- Docker por si alguien quiere probarlo sin instalar MySQL

## Cómo está organizado

La arquitectura es la típica de Spring: controller → service → repository, con DTOs para lo que entra y sale. Las entidades mapean las tablas con JPA, los servicios tienen la lógica del negocio, y los controladores exponen los endpoints REST con las rutas bajo /api.

Hay dos roles: ADMIN y CUSTOMER. Los endpoints que modifican productos o categorías requieren admin, el resto son públicos o solo piden estar logueado.
