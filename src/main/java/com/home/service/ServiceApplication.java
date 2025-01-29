package com.home.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
// @EnableScheduling
public class ServiceApplication {

	public static void main(String[] args) {

		// // Load environment variables from .env
		// Dotenv dotenv = Dotenv.configure().load();

		// // Start Spring Application
		// SpringApplication app = new SpringApplication(ServiceApplication.class);
		// ConfigurableEnvironment environment = app.run(args).getEnvironment();

		// // Set environment variables into Spring context
		// environment.getSystemProperties().put("DB_URL", dotenv.get("DB_URL"));
		// environment.getSystemProperties().put("DB_USERNAME",
		// dotenv.get("DB_USERNAME"));
		// environment.getSystemProperties().put("DB_PASSWORD",
		// dotenv.get("DB_PASSWORD"));
		// Load environment variables from .env
		// Dotenv dotenv = Dotenv.configure().load();
		// System.setProperty("DB_URL", dotenv.get("DB_URL"));
		// System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		// System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

		SpringApplication.run(ServiceApplication.class, args);
	}

}
