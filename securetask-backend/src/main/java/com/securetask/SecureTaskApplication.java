package com.securetask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SecureTaskApplication - Entry point for the Spring Boot application.
 *
 * @SpringBootApplication is a shortcut annotation that combines:
 *   - @Configuration      : marks this class as a source of bean definitions
 *   - @EnableAutoConfiguration : tells Spring Boot to automatically configure
 *                               the application based on the classpath
 *   - @ComponentScan     : tells Spring to scan this package and sub-packages
 *                          for @Component, @Service, @Repository, @Controller etc.
 *
 * SpringApplication.run() bootstraps the application, starts the embedded
 * Tomcat server, and begins accepting HTTP requests on port 8080.
 */
@SpringBootApplication
public class SecureTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureTaskApplication.class, args);
    }
}
