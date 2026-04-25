package com.project.academic_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class AcademicServiceApplication {

	/*public static void main(String[] args) {
//		Dotenv dotenv = Dotenv.load();
//		System.out.println(dotenv.get("MYSQL_USER"));
		SpringApplication.run(AcademicServiceApplication.class, args);
	}*/

	public static void main(String[] args) {
		String dbUrl, dbUsername, dbPassword;

		try {
			// Tentative de lecture via .env (utile en local)
			Dotenv dotenv = Dotenv.load();

			// dbUrl = Objects.requireNonNull(dotenv.get("DB_URL"));
			dbUrl = Objects.requireNonNull(dotenv.get("MYSQL_ACY_DB"));
			dbUsername = Objects.requireNonNull(dotenv.get("DB_USERNAME"));
			dbPassword = Objects.requireNonNull(dotenv.get("DB_PASSWORD"));

			System.out.println("Chargement des variables depuis .env ou System.getenv() terminé");

		} catch (Exception e) {
			// fallback en cas de souci avec dotenv
			System.out.println(" Impossible de charger .env, on utilise uniquement System.getenv()");
			dbUrl = System.getenv("MYSQL_ACY_DB");
			dbUsername = System.getenv("DB_USERNAME");
			dbPassword = System.getenv("DB_PASSWORD");

		}

		// Vérification des valeurs
		if (dbUrl == null || dbUsername == null || dbPassword == null ) {
			throw new IllegalStateException("Variables manquantes (DB_URL, DB_USERNAME, DB_PASSWORD)");
		}

		// Injection dans Spring
		System.setProperty("MYSQL_ACY_DB", dbUrl);
		System.setProperty("DB_USERNAME", dbUsername);
		System.setProperty("DB_PASSWORD", dbPassword);


		SpringApplication.run(AcademicServiceApplication.class, args);
	}


}
