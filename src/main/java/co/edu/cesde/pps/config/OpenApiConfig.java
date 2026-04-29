package co.edu.cesde.pps.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Configuración de Springdoc OpenAPI / Swagger UI.
 *
 * <h3>Esquema de autenticación</h3>
 * <p>Este proyecto usa <strong>Bearer token de sesión opaco</strong> (NO es JWT).
 * El token se genera en el backend al momento del login/registro y se almacena en la
 * tabla {@code user_sessions}. Cada request lo envía en el header {@code Authorization}
 * con el formato {@code Bearer <sessionToken>}.</p>
 *
 * <h3>Flujo para usar "Try it out" en Swagger UI</h3>
 * <ol>
 *   <li>Ejecutar {@code POST /api/v1/auth/login} o {@code POST /api/v1/auth/guest-session}.</li>
 *   <li>Copiar el campo {@code sessionToken} de la respuesta.</li>
 *   <li>Hacer clic en el botón <b>Authorize 🔓</b> en la parte superior de la UI.</li>
 *   <li>Pegar el token y confirmar — a partir de entonces todos los endpoints lo incluirán.</li>
 * </ol>
 *
 * <h3>Disponibilidad</h3>
 * <p>Esta configuración solo se activa con el perfil Spring {@code dev}.
 * Para habilitarlo: {@code --spring.profiles.active=dev} o
 * {@code SPRING_PROFILES_ACTIVE=dev} en el archivo {@code .env}.</p>
 *
 * <h3>URLs</h3>
 * <ul>
 *   <li>Swagger UI: {@code http://localhost:8081/swagger-ui.html}</li>
 *   <li>JSON OpenAPI: {@code http://localhost:8081/v3/api-docs}</li>
 * </ul>
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Product Purchasing System API",
                version = "1.0",
                description = """
                        API REST del sistema de compra de productos.
                        
                        ## Autenticación
                        Este API usa **Bearer Token de sesión opaco** (NO es JWT).
                        
                        1. Obtener un token vía `POST /api/v1/auth/login`, `POST /api/v1/auth/register`
                           o `POST /api/v1/auth/guest-session`.
                        2. Copiar el campo `sessionToken` de la respuesta.
                        3. Hacer clic en **Authorize 🔓** (esquina superior derecha) y pegar el token.
                        4. Todos los endpoints protegidos lo incluirán automáticamente.
                        
                        ## Roles
                        - **GUEST** — solo necesita sessionToken de `/auth/guest-session`
                        - **CUSTOMER** — sessionToken de `/auth/login` o `/auth/register`
                        - **ADMIN** — sessionToken de un usuario con rol ADMIN
                        """,
                contact = @Contact(
                        name = "CESDE — Equipo Backend",
                        email = "backend@cesde.edu.co"
                ),
                license = @License(name = "Uso académico — CESDE 2026")
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "Servidor local de desarrollo")
        }
)
@SecurityScheme(
        name = "BearerAuth",
        description = """
                Token de sesión opaco devuelto por /auth/login, /auth/register o /auth/guest-session.
                
                Formato del header: `Authorization: Bearer <sessionToken>`
                
                ⚠️ Este token NO es un JWT — es una cadena UUID almacenada en la base de datos
                en la tabla `user_sessions`. Su validez se verifica contra la BD en cada request.
                """,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "SessionToken",
        in = SecuritySchemeIn.HEADER
)
@Configuration
@Profile("dev")
public class OpenApiConfig {

    /**
     * Bean OpenAPI que define el orden de los grupos (tags) en la UI.
     * Los tags se listan en el orden en que deben aparecer en Swagger UI.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .tags(List.of(
                        new Tag().name("Auth")
                                .description("Autenticación — sesión de invitado, registro, login y logout"),
                        new Tag().name("Products")
                                .description("Catálogo de productos — consulta pública (no requiere token)"),
                        new Tag().name("Categories")
                                .description("Categorías del catálogo — consulta pública (no requiere token)"),
                        new Tag().name("Cart")
                                .description("Carrito de compras — requiere sessionToken (guest o autenticado)"),
                        new Tag().name("Orders")
                                .description("Órdenes de compra — requiere sessionToken de usuario autenticado"),
                        new Tag().name("User Profile")
                                .description("Perfil del usuario autenticado — datos personales y contraseña"),
                        new Tag().name("Addresses")
                                .description("Direcciones del usuario autenticado — envío y facturación"),
                        new Tag().name("Admin — Products")
                                .description("Administración de productos — requiere sessionToken con rol ADMIN"),
                        new Tag().name("Admin — Users")
                                .description("Administración de usuarios — requiere sessionToken con rol ADMIN")
                ));
    }
}

