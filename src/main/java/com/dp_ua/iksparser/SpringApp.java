package com.dp_ua.iksparser;

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
    // барьеры
//    private final String URL = "https://iks.org.ua/competitions1/en/2024.01.12_kyiv/live?s=D6439A95-F3D4-4E71-B1EA-084D7B356947";
    // пантера
    private final String URL = "https://iks.org.ua/competitions1/en/2024.01.13_kyiv/live?s=0257D292-2228-4316-9141-DF185D18CDCF";


    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        app.start(URL, surname);
    }
}
