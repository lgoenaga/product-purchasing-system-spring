package co.edu.cesde.pps.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Carga variable desde el archivo .env a System properties antes de iniciar Spring Boot.
 *
 * <p>Se usa solo como ayuda para desarrollo local. Si una variable ya existe como
 * variable de entorno real o como system property, NO se sobrescribe.</p>
 */
public final class DotenvDevelopmentLoader {

    private static final Logger log = LoggerFactory.getLogger(DotenvDevelopmentLoader.class);

    private static final String SPRING_PROFILES_ACTIVE_ENV = "SPRING_PROFILES_ACTIVE";
    private static final String SPRING_PROFILES_ACTIVE_PROPERTY = "spring.profiles.active";

    private static final List<String> SUPPORTED_KEYS = List.of(
        "DB_HOST",
        "DB_PORT",
        "DB_NAME",
        "DB_USER",
        "DB_PASSWORD",
        "DB_DDL_AUTO",
        "DB_SHOW_SQL",
        "DB_POOL_SIZE",
        "SERVER_PORT",
        "APP_ENVIRONMENT",
        "CORS_ALLOWED_ORIGINS",
        "LOG_LEVEL",
        "LOG_SQL_LEVEL",
        "LOG_SQL_BIND_LEVEL",
        SPRING_PROFILES_ACTIVE_ENV
    );

    private static final Set<String> DEVELOPMENT_ENVIRONMENTS = Set.of(
        "development",
        "dev",
        "local"
    );

    private DotenvDevelopmentLoader() {
        throw new AssertionError("DotenvDevelopmentLoader is a utility class and cannot be instantiated");
    }

    public static void load() {
        try {
            String workingDir = System.getProperty("user.dir");
            log.info("Buscando .env en directorio: {}", workingDir);

            Dotenv dotenv = Dotenv.configure()
                .directory(workingDir)
                .filename(".env")
                .ignoreIfMissing()
                .ignoreIfMalformed()
                .load();

            int loadedProperties = 0;
            for (String key : SUPPORTED_KEYS) {
                loadedProperties += applyPropertyIfMissing(dotenv, key);
            }

            loadedProperties += applyDevProfileFallbackFromAppEnvironment(dotenv);

            if (loadedProperties > 0) {
                log.info("Se cargaron {} propiedades desde .env en: {}", loadedProperties, workingDir);
            } else {
                log.warn("No se cargaron propiedades desde .env — verifica que el archivo exista en: {}", workingDir);
            }
        } catch (Exception exception) {
            log.warn("No se pudo cargar el archivo .env. La aplicación continuará con la configuración disponible: {}",
                exception.getMessage());
            log.debug("Detalle de la carga de .env", exception);
        }
    }

    private static int applyPropertyIfMissing(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            return 0;
        }

        String systemPropertyKey = mapToSystemPropertyKey(key);

        if (System.getenv(key) != null
                || System.getProperty(key) != null
                || System.getProperty(systemPropertyKey) != null) {
            return 0;
        }

        System.setProperty(systemPropertyKey, value);
        return 1;
    }

    private static int applyDevProfileFallbackFromAppEnvironment(Dotenv dotenv) {
        if (System.getenv(SPRING_PROFILES_ACTIVE_ENV) != null
                || System.getProperty(SPRING_PROFILES_ACTIVE_ENV) != null
                || System.getProperty(SPRING_PROFILES_ACTIVE_PROPERTY) != null) {
            return 0;
        }

        String appEnvironment = dotenv.get("APP_ENVIRONMENT");
        if (appEnvironment == null || appEnvironment.isBlank()) {
            return 0;
        }

        String normalized = appEnvironment.trim().toLowerCase(Locale.ROOT);
        if (!DEVELOPMENT_ENVIRONMENTS.contains(normalized)) {
            return 0;
        }

        System.setProperty(SPRING_PROFILES_ACTIVE_PROPERTY, "dev");
        log.info("Perfil Spring '{}' activado automáticamente a partir de APP_ENVIRONMENT={}",
                "dev", appEnvironment);
        return 1;
    }

    private static String mapToSystemPropertyKey(String key) {
        if (SPRING_PROFILES_ACTIVE_ENV.equals(key)) {
            return SPRING_PROFILES_ACTIVE_PROPERTY;
        }
        return key;
    }
}

