package com.usbank.auth.client;


import com.usbank.auth.config.MicrosoftAuthConfig;
import com.usbank.auth.exception.MicrosoftAuthException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client responsible for executing Microsoft authentication requests.
 */
public class MicrosoftAuthClient {
    private final int connectionRequestTimeout;
    private final int connectTimeout;
    private final int socketTimeout;
    private final RequestConfig requestConfig;

    public MicrosoftAuthClient(int connectionRequestTimeout, int connectTimeout, int socketTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.requestConfig = createRequestConfig();
    }

    public String executeAuthRequest() throws MicrosoftAuthException {
        try (CloseableHttpClient httpClient = createHttpClient(requestConfig)) {
            HttpPost httpPost = prepareHttpPostRequest();
            return executeRequest(httpClient, httpPost);
        } catch (IOException e) {
            throw handleSpecificIOException(e);
        }
    }

    /**
     * Creates RequestConfig with predefined timeout settings.
     *
     * @return Configured RequestConfig instance
     */
    private RequestConfig createRequestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .build();
    }

    /**
     * Creates HttpClient with custom request configuration.
     *
     * @param requestConfig Timeout and connection settings
     * @return Configured CloseableHttpClient
     */
    private CloseableHttpClient createHttpClient(RequestConfig requestConfig) {
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * Prepares HTTP POST request with authentication parameters.
     *
     * @return Configured HttpPost request
     * @throws MicrosoftAuthException if parameter preparation fails
     */
    private HttpPost prepareHttpPostRequest() throws MicrosoftAuthException {
        try {
            HttpPost httpPost = new HttpPost(MicrosoftAuthConfig.getMicrosoftLoginUrl());
            httpPost.setEntity(prepareRequestParams());
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            return httpPost;
        } catch (IOException e) {
            throw new MicrosoftAuthException(
                    "Failed to prepare authentication request", e
            );
        }
    }

    /**
     * Prepares URL-encoded form parameters for authentication request.
     *
     * @return UrlEncodedFormEntity with authentication parameters
     * @throws IOException if parameter encoding fails
     */
    private UrlEncodedFormEntity prepareRequestParams() throws IOException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", MicrosoftAuthConfig.getClientId()));
        params.add(new BasicNameValuePair("client_secret", MicrosoftAuthConfig.getClientSecret()));
        params.add(new BasicNameValuePair("tenant", MicrosoftAuthConfig.getTenantId()));
        params.add(new BasicNameValuePair("grant_type", "client_credentials"));
        params.add(new BasicNameValuePair("scope", "https://graph.microsoft.com/.default"));

        return new UrlEncodedFormEntity(params);
    }

    /**
     * Executes authentication request and processes response.
     *
     * @param httpClient Configured HTTP client
     * @param httpPost   Prepared HTTP POST request
     * @return Authentication response
     * @throws MicrosoftAuthException for various authentication failures
     */
    private String executeRequest(CloseableHttpClient httpClient, HttpPost httpPost)
            throws MicrosoftAuthException {
        try {
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity);
            } else {
                throw new MicrosoftAuthException(
                        "Authentication failed with status code: " + statusCode
                );
            }
        } catch (SocketTimeoutException e) {
            throw new MicrosoftAuthException(
                    "Connection timed out during authentication", e
            );
        } catch (IOException e) {
            throw new MicrosoftAuthException(
                    "Network error during authentication", e
            );
        }
    }

    /**
     * Handles specific IO exceptions with detailed error mapping.
     *
     * @param exception Original IOException
     * @return Mapped MicrosoftAuthException
     */
    private MicrosoftAuthException handleSpecificIOException(IOException exception) {
        if (exception instanceof SocketTimeoutException) {
            return new MicrosoftAuthException(
                    "Socket connection timeout", exception
            );
        } else if (exception instanceof ConnectTimeoutException) {
            return new MicrosoftAuthException(
                    "Connection establishment timeout", exception
            );
        }

        return new MicrosoftAuthException(
                "Unexpected network error", exception
        );
    }
}
