TeamMaravillaApi
API REST desarrollada con Java 21 y Spring Boot 3.5.16 para la segunda iteración de TeamMaravillaApp, una aplicación Android de listas de la compra y recetas.

El objetivo es sustituir progresivamente la API anterior por un backend propio, con persistencia relacional, autenticación segura y una arquitectura mantenible, conservando las funcionalidades existentes de Android y su enfoque offline-first.
Estado del proyecto: en desarrollo. La autenticación, el catálogo de productos y recetas y la persistencia inicial de listas ya están implementados. La integración de Android con esta API y las operaciones completas de listas siguen pendientes.

Tecnologías
Área	Tecnología
Lenguaje	Java 21
Framework	Spring Boot 3.5.16
Gestión de dependencias	Maven
API HTTP	Spring Web, Jakarta Validation
Persistencia	Spring Data JPA, Hibernate, MySQL 8
Evolución del esquema	Flyway
Seguridad	Spring Security, BCrypt, JWT (JJWT 0.13.0)
Pruebas	JUnit 5, Spring Boot Test, MockMvc


Funcionalidades implementadas
- Usuarios: persistencia relacional, registro con validación, unicidad de usuario/correo y contraseñas almacenadas mediante hash BCrypt.
- Autenticación: inicio de sesión, emisión de JWT firmados con HS256 y consulta de la identidad del usuario autenticado.
- Protección de endpoints: API sin sesión de servidor (stateless), con tokens Bearer enviados mediante la cabecera Authorization.
- Catálogo: almacenamiento y consulta de productos y recetas; carga de datos iniciales mediante migraciones.
- Listas de compra: entidad, relación con el propietario, repositorio y persistencia en MySQL. Todavía no dispone de endpoints REST completos.
- Base de datos: migraciones Flyway versionadas de V1 a V7; Hibernate valida el esquema en lugar de generarlo automáticamente.
- Pruebas: pruebas de repositorios, servicios, autenticación y endpoints, incluidos escenarios de error y seguridad.
Endpoints disponibles
Método	Ruta	Autenticación	Función
POST	/api/auth/register	No	Registrar un usuario
POST	/api/auth/login	No	Validar credenciales y obtener un JWT
GET	/api/auth/me	Sí	Consultar el usuario asociado al token
GET	/api/products	Sí	Listar productos
GET	/api/recipes	Sí	Listar recetas
GET	/api/recipes/{id}	Sí	Consultar el detalle de una receta


Salvo los endpoints públicos de registro e inicio de sesión, la configuración actual de Spring Security exige autenticación para las solicitudes. Una petición protegida debe incluir:
Authorization: Bearer <accessToken>
El registro devuelve los datos públicos del nuevo usuario. El inicio de sesión devuelve accessToken, tokenType, expiresIn y user. La autorización por propiedad de listas y favoritos se completará al incorporar las operaciones REST correspondientes.
Puesta en marcha local
Requisitos
- JDK 21.
- MySQL 8 en ejecución y una base de datos creada para el proyecto.
- Acceso a Maven mediante el wrapper incluido (mvnw o mvnw.cmd).
Variables de entorno
La aplicación obtiene su configuración de variables de entorno. No deben añadirse contraseñas ni claves JWT al repositorio.
Variable	Descripción
DB_URL	URL JDBC de MySQL
DB_USERNAME	Usuario de MySQL
DB_PASSWORD	Contraseña de MySQL
JWT_SECRET	Clave aleatoria de 32 bytes, codificada en Base64
JWT_EXPIRATION	Duración ISO-8601 del JWT

$env:DB_URL = "jdbc:mysql://localhost:3306/teammaravilla"
$env:DB_USERNAME = "USUARIO_LOCAL"
$env:DB_PASSWORD = "CONTRASENA_LOCAL"
$env:JWT_SECRET = "BASE64_DE_32_BYTES_ALEATORIOS"
$env:JWT_EXPIRATION = "PT15M"

.\mvnw.cmd spring-boot:run
JWT_SECRET debe contener una clave real válida generada mediante un generador criptográficamente seguro. El valor ilustrativo anterior no sirve para arrancar la aplicación. Las variables se establecen en la sesión de terminal; no se cargan automáticamente desde un archivo .env.

Al arrancar, Flyway aplica las migraciones pendientes y Hibernate comprueba que el esquema coincide con las entidades (ddl-auto=validate).
Organización del código
src/
├── main/
│   ├── java/com/teammaravilla/api/
│   │   ├── auth/          # Registro, login, JWT y Spring Security
│   │   ├── user/          # Entidad y repositorio de usuarios
│   │   ├── product/       # Persistencia y consulta de productos
│   │   ├── recipe/        # Persistencia y consulta de recetas
│   │   └── shoppinglist/  # Modelo y repositorio de listas
│   └── resources/
│       ├── application.properties
│       └── db/migration/ # Migraciones V1–V7
└── test/java/com/teammaravilla/api/  # Pruebas automatizadas
Próximas etapas
1. Persistencia de elementos de las listas y operaciones REST de listas, con autorización por propietario.
2. Persistencia y operaciones de favoritos de recetas por usuario.
3. Adaptación del cliente Android a los nuevos contratos HTTP y tokens JWT, sin perder la persistencia local con Room.
4. Revisión de sincronización, gestión segura de la sesión, errores y pruebas de integración con Android.
5. Evaluación de Docker, despliegue y documentación final según las necesidades reales del proyecto.
Aplicación Android
El cliente móvil se desarrolla por separado y está disponible en TeamMaravillaApp. La aplicación Android actual mantiene funcionalidades y persistencia local propias; su migración a esta nueva API se realizará progresivamente.
