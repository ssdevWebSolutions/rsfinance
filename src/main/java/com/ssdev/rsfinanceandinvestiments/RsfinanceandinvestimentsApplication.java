package com.ssdev.rsfinanceandinvestiments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RsfinanceandinvestimentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(RsfinanceandinvestimentsApplication.class, args);
	}

}
