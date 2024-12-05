server:
  ssl:
    key-store: classpath:your-keystore.jks
    key-store-password: your-password
    key-password: your-key-password

feign:
  httpclient:
    enabled: true

custom:
  ssl:
    trust-store: classpath:your-keystore.jks
    trust-store-password: your-password


    import feign.Client;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.KeyStore;

@Configuration
public class FeignClientConfig {

    @Bean
    public Client feignClient() throws Exception {
        // Load the JKS
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(getClass().getResourceAsStream("/your-keystore.jks"),
                      "your-password".toCharArray());

        // Configure SSLContext
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, "your-key-password".toCharArray())
                .build();

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();

        return new Client.Default(httpClient, null);
    }
}
