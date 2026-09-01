package com.example.Cambell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CambellApplication {

	public static void main(String[] args) {
		SpringApplication.run(CambellApplication.class, args);
	}

}
