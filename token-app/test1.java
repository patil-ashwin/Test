import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

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

            if (environment instanceof ConfigurableEnvironment configurableEnvironment) {
                log.info("### Logging Spring Environment Property Sources ###");
                for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
                    log.info("Property Source: {}", propertySource.getName());
                    if (propertySource.getSource() instanceof Map) {
                        ((Map<?, ?>) propertySource.getSource()).forEach((key, value) ->
                                log.info("{}={}", key, value));
                    }
                }
            } else {
                log.warn("Environment is not ConfigurableEnvironment; property sources cannot be logged.");
            }
        };
    }
}