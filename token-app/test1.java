// TokenResponse.java
package com.example.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}

// JwtTokenValidator.java
package com.example.auth.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenValidator {
    public boolean isValidToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .parseClaimsJwt(token)
                    .getBody();

            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}

// MicrosoftAuthFilter.java
package com.example.auth.filter;

import com.example.auth.model.TokenResponse;
import com.example.auth.service.MicrosoftAuthService;
import com.example.auth.util.JwtTokenValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class MicrosoftAuthFilter implements WebFilter {
    private static final String TOKEN_CACHE_KEY = "microsoft_access_token";
    private final Cache<String, TokenResponse> tokenCache;

    @Autowired
    private MicrosoftAuthService microsoftAuthService;

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    private final ObjectMapper objectMapper;

    public MicrosoftAuthFilter() {
        this.tokenCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(1))
                .maximumSize(1)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.fromSupplier(() -> {
                    TokenResponse cachedToken = tokenCache.getIfPresent(TOKEN_CACHE_KEY);

                    if (cachedToken != null &&
                            jwtTokenValidator.isValidToken(cachedToken.getAccessToken())) {
                        System.out.println("Using cached token");
                        return cachedToken.getAccessToken();
                    }
                    return null;
                })
                .flatMap(token -> token != null
                        ? Mono.just(token)
                        : fetchAndCacheNewToken())
                .doOnNext(token -> {
                    System.out.println("Authenticated token: " + token);
                    // You can add token to request headers or perform other actions
                })
                .then(chain.filter(exchange));
    }

    private Mono<String> fetchAndCacheNewToken() {
        return Mono.fromCallable(() -> {
            try {
                String authResponse = microsoftAuthService.authenticateMicrosoftAccount();
                TokenResponse tokenResponse = objectMapper.readValue(authResponse, TokenResponse.class);

                tokenCache.put(TOKEN_CACHE_KEY, tokenResponse);
                return tokenResponse.getAccessToken();
            } catch (Exception e) {
                System.err.println("Token retrieval failed: " + e.getMessage());
                throw new RuntimeException("Failed to retrieve access token", e);
            }
        });
    }
}


// MicrosoftAuthFilter.java
package com.example.auth.filter;

import com.example.auth.model.TokenResponse;
import com.example.auth.service.MicrosoftAuthService;
import com.example.auth.util.JwtTokenValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class MicrosoftAuthFilter implements WebFilter {
    private static final String TOKEN_CACHE_NAME = "microsoftTokenCache";
    private static final String TOKEN_CACHE_KEY = "microsoft_access_token";

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MicrosoftAuthService microsoftAuthService;

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    private final ObjectMapper objectMapper;

    public MicrosoftAuthFilter() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.fromSupplier(() -> {
            Cache cache = cacheManager.getCache(TOKEN_CACHE_NAME);
            TokenResponse cachedToken = cache != null 
                ? cache.get(TOKEN_CACHE_KEY, TokenResponse.class) 
                : null;
            
            if (cachedToken != null && 
                jwtTokenValidator.isValidToken(cachedToken.getAccessToken())) {
                System.out.println("Using cached token");
                return cachedToken.getAccessToken();
            }
            return null;
        })
        .flatMap(token -> token != null 
            ? Mono.just(token) 
            : fetchAndCacheNewToken())
        .doOnNext(token -> {
            System.out.println("Authenticated token: " + token);
            // You can add token to request headers or perform other actions
        })
        .then(chain.filter(exchange));
    }

    private Mono<String> fetchAndCacheNewToken() {
        return Mono.fromCallable(() -> {
            try {
                String authResponse = microsoftAuthService.authenticateMicrosoftAccount();
                TokenResponse tokenResponse = objectMapper.readValue(authResponse, TokenResponse.class);
                
                // Store in cache
                Cache cache = cacheManager.getCache(TOKEN_CACHE_NAME);
                if (cache != null) {
                    cache.put(TOKEN_CACHE_KEY, tokenResponse);
                }
                
                return tokenResponse.getAccessToken();
            } catch (Exception e) {
                System.err.println("Token retrieval failed: " + e.getMessage());
                throw new RuntimeException("Failed to retrieve access token", e);
            }
        });
    }
}
