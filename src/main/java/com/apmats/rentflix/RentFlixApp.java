package com.apmats.rentflix;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RentFlixApp {

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}

	public static void main(String[] args) {
		SpringApplication.run(RentFlixApp.class, args);
	}
}
