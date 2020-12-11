package com.autentia.dockerspringnetflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class DockerSpringNetflixEurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DockerSpringNetflixEurekaServerApplication.class, args);
	}

}
