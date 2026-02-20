package com.dp_ua.iksparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j(topic = "APP")
public class SpringApp {
    public static final int ORDER_FOR_COMPETITION_UPDATER = 2; // start check and update competition after all beans are initialized
    public static final int ORDER_FOR_APP_AND_BOT_STARTER = 1; // should start after all beans are initialized

    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class, args);
    }
}
