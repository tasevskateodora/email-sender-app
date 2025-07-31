package com.example.iwemailsender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableScheduling
@EnableRetry
public class IwEmailSenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(IwEmailSenderApplication.class, args);
    }

}
