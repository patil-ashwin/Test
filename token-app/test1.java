import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        return logResponse(response);
    }

    private void logRequest(HttpRequest request, byte[] body) {
        log.info("Request URI: {}", request.getURI());
        log.info("Request Method: {}", request.getMethod());
        log.info("Request Headers: {}", request.getHeaders());
        log.info("Request Body: {}", new String(body, StandardCharsets.UTF_8));
    }

    private ClientHttpResponse logResponse(ClientHttpResponse response) throws IOException {
        // Buffer the response body
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputStream responseBodyStream = response.getBody();
        if (responseBodyStream != null) {
            responseBodyStream.transferTo(buffer);
        }

        String responseBody = buffer.toString(StandardCharsets.UTF_8);
        log.info("Response Status Code: {}", response.getStatusCode());
        log.info("Response Headers: {}", response.getHeaders());
        log.info("Response Body: {}", responseBody);

        // Return a new response with the buffered body
        return new BufferingClientHttpResponse(response, buffer.toByteArray());
    }

    // Custom wrapper to allow the body to be re-read
    private static class BufferingClientHttpResponse implements ClientHttpResponse {
        private final ClientHttpResponse original;
        private final byte[] body;

        public BufferingClientHttpResponse(ClientHttpResponse original, byte[] body) {
            this.original = original;
            this.body = body;
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpHeaders getHeaders() {
            return original.getHeaders();
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return original.getStatusCode();
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return original.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return original.getStatusText();
        }

        @Override
        public void close() {
            original.close();
        }
    }
}