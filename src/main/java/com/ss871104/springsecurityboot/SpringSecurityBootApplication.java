package com.ss871104.springsecurityboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class SpringSecurityBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityBootApplication.class, args);
    }

}
