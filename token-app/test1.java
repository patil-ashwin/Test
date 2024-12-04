import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Properties;

@Configuration
public class EnvironmentLoggerConfig {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentLoggerConfig.class);

    @Bean
    public CommandLineRunner logEnvironmentVariables(Environment environment) {
        return args -> {
            log.info("### Logging Environment Variables ###");
            Map<String, String> envVars = System.getenv();
            envVars.forEach((key, value) -> log.info("{}={}", key, value));

            log.info("### Logging System Properties ###");
            Properties systemProperties = System.getProperties();
            systemProperties.forEach((key, value) -> log.info("{}={}", key, value));

            log.info("### Logging Spring Environment Properties ###");
            for (String propertyName : environment.getPropertySources().toString().split("\n")) {
                log.info(propertyName.trim());
            }
        };
    }
}