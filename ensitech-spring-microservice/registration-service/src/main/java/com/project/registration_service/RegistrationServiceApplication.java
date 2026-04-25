package com.project.registration_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.Objects;

@SpringBootApplication
@EnableFeignClients
public class RegistrationServiceApplication {


	/*public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.out.println(dotenv.get("MYSQL_DATABASE"));


		SpringApplication.run(RegistrationServiceApplication.class, args);

//		System.setProperty("MYSQL_DATABASE", Objects.requireNonNull(dotenv.get("MYSQL_DATABASE")));
//		System.setProperty("MYSQL_USERNAME", Objects.requireNonNull(dotenv.get("MYSQL_USERNAME")));
//		System.setProperty("MYSQL_PASSWORD", Objects.requireNonNull(dotenv.get("MYSQL_PASSWORD")));
	}*/
	public static void main(String[] args) {
		String dbUrl, dbUsername, dbPassword;

		try {
			// Tentative de lecture via .env (utile en local)
			Dotenv dotenv = Dotenv.load();

			// dbUrl = Objects.requireNonNull(dotenv.get("DB_URL"));
			dbUrl = Objects.requireNonNull(dotenv.get("MYSQL_REG_DB"));
			dbUsername = Objects.requireNonNull(dotenv.get("DB_USERNAME"));
			dbPassword = Objects.requireNonNull(dotenv.get("DB_PASSWORD"));

			System.out.println("Chargement des variables depuis .env ou System.getenv() terminé");

		} catch (Exception e) {
			// fallback en cas de souci avec dotenv
			System.out.println(" Impossible de charger .env, on utilise uniquement System.getenv()");
			dbUrl = System.getenv("MYSQL_REG_DB");
			dbUsername = System.getenv("DB_USERNAME");
			dbPassword = System.getenv("DB_PASSWORD");

		}

		// Vérification des valeurs
		if (dbUrl == null || dbUsername == null || dbPassword == null ) {
			throw new IllegalStateException("Variables manquantes (DB_URL, DB_USERNAME, DB_PASSWORD)");
		}

		// Injection dans Spring
		System.setProperty("MYSQL_REG_DB", dbUrl);
		System.setProperty("DB_USERNAME", dbUsername);
		System.setProperty("DB_PASSWORD", dbPassword);


		SpringApplication.run(RegistrationServiceApplication.class, args);
	}


}
