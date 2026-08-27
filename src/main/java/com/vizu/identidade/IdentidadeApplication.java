package com.vizu.identidade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class IdentidadeApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentidadeApplication.class, args);
	}

}
