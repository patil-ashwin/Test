import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class FeignClientConfig {

    @Bean
    public Client feignClient() throws Exception {
        // Load your JKS file
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keyStoreStream = getClass().getResourceAsStream("/your-keystore.jks")) {
            keyStore.load(keyStoreStream, "your-password".toCharArray());
        }

        // Initialize TrustManager with the JKS
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        // Create SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        // Use Feign Client with the SSL Socket Factory
        return new Client.Default(sslContext.getSocketFactory(), null);
    }
}
