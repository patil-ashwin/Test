import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class EnvironmentLogger {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentLogger.class);

    private final Environment environment;

    public EnvironmentLogger(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void logEnvironment() {
        log.info("### Logging Environment Variables ###");
        Map<String, String> envVars = System.getenv();
        envVars.forEach((key, value) -> log.info("{}={}", key, value));

        log.info("### Logging Spring Environment Properties ###");
        environment.getSystemProperties().forEach((key, value) -> log.info("{}={}", key, value));
    }
}