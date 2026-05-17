package com.backend_desigeo.desigeo_auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DesigeoAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DesigeoAuthServiceApplication.class, args);
	}

}
