package com.autentia.dockerspringnetflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class DockerSpringNetflixGreetingMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DockerSpringNetflixGreetingMicroserviceApplication.class, args);
	}

}
