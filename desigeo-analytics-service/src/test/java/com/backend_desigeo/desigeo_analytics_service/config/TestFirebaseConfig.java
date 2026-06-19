package com.backend_desigeo.desigeo_analytics_service.config;

import com.google.firebase.FirebaseApp;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Configuración Firebase para tests.
 *
 * Reemplaza FirebaseConfig en el contexto de test para evitar que Spring
 * intente cargar serviceAccountKey.json del classpath, que no existe en el
 * entorno de pruebas. Provee un mock de FirebaseApp en su lugar.
 *
 * Uso: incluir via @Import(TestFirebaseConfig.class) en tests @SpringBootTest,
 * o dejar que el @MockBean en DesigeoAnalyticsServiceApplicationTests lo cubra.
 */
@TestConfiguration
public class TestFirebaseConfig {

    @Bean
    @Primary
    public FirebaseApp firebaseApp() {
        return Mockito.mock(FirebaseApp.class);
    }
}
