package com.agrinex;

import java.util.Arrays;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;

public class TestAgrinexBackendApplication {

	public static void main(String[] args) {
		// Containers replace the local services; the local profile supplies the rest (StartupProfileValidator).
		String[] withProfile = Stream.concat(Stream.of("--spring.profiles.active=local"), Arrays.stream(args))
			.toArray(String[]::new);
		SpringApplication.from(AgrinexBackendApplication::main).with(TestcontainersConfiguration.class).run(withProfile);
	}

}
