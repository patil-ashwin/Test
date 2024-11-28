package com.usbank.auth.service;

import com.usbank.auth.client.MicrosoftAuthClient;
import com.usbank.auth.exception.MicrosoftAuthException;

/**
 * Service layer for Microsoft authentication operations.
 */
public class MicrosoftAuthService {
    private final MicrosoftAuthClient authClient;

    /**
     * Constructs MicrosoftAuthService with default client.
     */
    public MicrosoftAuthService(int connectionRequestTimeout, int connectTimeout, int socketTimeout) {
        this.authClient = new MicrosoftAuthClient(connectionRequestTimeout,connectTimeout,socketTimeout);
    }

    /**
     * Authenticates with Microsoft and returns authentication response.
     *
     * @return Authentication response string
     * @throws MicrosoftAuthException if authentication fails
     */
    public String authenticateMicrosoftAccount() throws MicrosoftAuthException {
        return authClient.executeAuthRequest();
    }
}