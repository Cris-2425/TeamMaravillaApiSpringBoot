# TeamMaravillaApi

Backend independiente de TeamMaravillaApp. Primer commit: inicialización con Java 21, Spring Boot 3.5.16 y Maven Wrapper.

## Alcance

Incluye únicamente Spring Web y Validation como dependencias de producción.
Incluye spring-boot-starter-test para el test de contexto generado por Initializr.
No incluye entidades, tablas, migraciones SQL, endpoints, seguridad ni integración Android.

## Comprobación en PowerShell

```powershell
java -version
javac -version
.\mvnw.cmd -version
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Se necesita un JDK 21 y conexión para descargar Maven y dependencias.
La compilación, el test contextLoads y el arranque no requieren datasource.
El servidor arranca en el puerto 8080. No hay endpoints propios: una petición a
la raíz devuelve 404. Para detenerlo, pulsa Ctrl+C.
Para empaquetar ejecutando también las pruebas: `.\mvnw.cmd clean package`.

## Versiones

Spring Boot 3.5.16 es estable y compatible con Java 21. La publicación oficial la
identifica como la última versión OSS de la serie 3.5. Se conserva 3.x por decisión
del proyecto. Referencia: https://spring.io/blog/2026/06/25/spring-boot-3-5-16-available-now/

La estructura y los scripts del wrapper proceden de Initializr. El POM se ha
adaptado a 3.5.16 porque el generador ya no acepta esa versión directamente.

## Siguiente commit

`feat: configure database and Flyway`

Añadir conjuntamente Spring Data JPA, MySQL Driver, Flyway Core y Flyway MySQL,
y configurar el datasource. Este segundo commit todavía no está implementado.
