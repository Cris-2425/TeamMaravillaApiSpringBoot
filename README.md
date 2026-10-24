# TeamMaravillaApi

Backend independiente de TeamMaravillaApp. Primer commit: inicialización con Java 21, Spring Boot 3.5.16 y Maven Wrapper.

## Versiones

Spring Boot 3.5.16 es estable y compatible con Java 21.
## Siguiente commit

`feat: configure database and Flyway`

Añadir conjuntamente Spring Data JPA, MySQL Driver, Flyway Core y Flyway MySQL,
y configurar el datasource.

## Alcance de este commit

`feat: configure database and Flyway`

Añade Spring Data JPA, MySQL Driver, Flyway Core y Flyway MySQL. Se mantienen
Spring Web, Validation y Spring Boot Starter Test. Spring Boot gestiona las versiones.
No incluye entidades, tablas funcionales, endpoints, seguridad ni integración Android.

