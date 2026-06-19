package com.backend_desigeo.desigeo_analytics_service;

import com.backend_desigeo.desigeo_analytics_service.config.TestFirebaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
		// DataSource — H2 en memoria, evita necesidad de PostgreSQL
		"spring.datasource.url=jdbc:h2:mem:analyticstest;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		// JWT dummy
		"jwt.secret=test-secret-key-for-unit-tests-minimum-256-bits-padding-here-long",
		"jwt.expiration-seconds=3600",
		// Redis deshabilitado
		"spring.autoconfigure.exclude=" +
				"org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
				"org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
		// Evitar que spring-dotenv falle al no encontrar .env
		"spring.config.import="
})
@Import(TestFirebaseConfig.class)
class DesigeoAnalyticsServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
