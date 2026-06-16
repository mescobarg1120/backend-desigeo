package com.backend_desigeo.desigeo_notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DesigeoNotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DesigeoNotificationServiceApplication.class, args);
	}

}
