package com.Backend.SecondBrainProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SecondBrainProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecondBrainProjectApplication.class, args);
	}

}
