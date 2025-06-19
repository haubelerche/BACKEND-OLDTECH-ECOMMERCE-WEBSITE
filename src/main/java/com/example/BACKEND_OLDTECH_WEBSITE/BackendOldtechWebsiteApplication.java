package com.example.BACKEND_OLDTECH_WEBSITE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendOldtechWebsiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendOldtechWebsiteApplication.class, args);
	}

}
