package com.autentia.dockerspringnetflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringBootApplication
public class DockerSpringNetflixZuulServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DockerSpringNetflixZuulServiceApplication.class, args);
	}

}
