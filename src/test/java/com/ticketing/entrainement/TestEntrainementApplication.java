package com.ticketing.entrainement;

import org.springframework.boot.SpringApplication;

public class TestEntrainementApplication {

	public static void main(String[] args) {
		SpringApplication.from(EntrainementApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
