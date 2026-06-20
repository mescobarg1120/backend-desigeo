package com.backend_desigeo.desigeo_analytics_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_TYPE}")
    private String type;

    @Value("${FIREBASE_PROJECT_ID}")
    private String firebaseProjectId;

    @Value("${FIREBASE_PRIVATE_KEY_ID}")
    private String privateKeyId;

    @Value("${FIREBASE_PRIVATE_KEY}")
    private String privateKey;

    @Value("${FIREBASE_CLIENT_EMAIL}")
    private String clientEmail;

    @Value("${FIREBASE_CLIENT_ID}")
    private String clientId;

    @Value("${FIREBASE_AUTH_URI}")
    private String authUri;

    @Value("${FIREBASE_TOKEN_URI}")
    private String tokenUri;

    @Value("${FIREBASE_AUTH_PROVIDER_CERT_URL}")
    private String authProviderCertUrl;

    @Value("${FIREBASE_CLIENT_CERT_URL}")
    private String clientCertUrl;

    @Bean
    @ConditionalOnMissingBean(FirebaseApp.class)
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        InputStream serviceAccount = new ByteArrayInputStream(buildCredentialsJson().getBytes(StandardCharsets.UTF_8));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        return FirebaseApp.initializeApp(options);
    }

    private String buildCredentialsJson() {
        String fixedPrivateKey = privateKey.replace("\\n", "\n");

        return """
                {
                  "type": "%s",
                  "project_id": "%s",
                  "private_key_id": "%s",
                  "private_key": "%s",
                  "client_email": "%s",
                  "client_id": "%s",
                  "auth_uri": "%s",
                  "token_uri": "%s",
                  "auth_provider_x509_cert_url": "%s",
                  "client_x509_cert_url": "%s",
                  "universe_domain": "googleapis.com"
                }
                """.formatted(type, firebaseProjectId, privateKeyId, fixedPrivateKey,
                clientEmail, clientId, authUri, tokenUri, authProviderCertUrl, clientCertUrl);
    }
}
