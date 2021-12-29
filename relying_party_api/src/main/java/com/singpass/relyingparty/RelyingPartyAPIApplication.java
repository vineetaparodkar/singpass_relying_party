package com.singpass.relyingparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot main application class.
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan({"com.singpass.relyingparty"})
public class RelyingPartyAPIApplication {

    public static void main(String[] args) {
        SpringApplication.run(RelyingPartyAPIApplication.class, args);
    }

}
