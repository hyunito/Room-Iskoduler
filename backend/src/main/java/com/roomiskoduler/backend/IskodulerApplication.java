package com.roomiskoduler.backend;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
public class IskodulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IskodulerApplication.class, args);
	}

}
