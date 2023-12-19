package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j(topic = "APP")
public class SpringApp implements CommandLineRunner {
    @Autowired
    private App app;
    private final String surname = "Решетило";
    private final String URL = "https://iks.org.ua/competitions1/en/2023.12.15-16_kyiv/live?s=333DE691-FB1E-4E01-B46C-1F52A5D9D6CC";


    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        app.start(URL, surname);
    }
}
