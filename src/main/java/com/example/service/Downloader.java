package com.example.service;

import lombok.extern.java.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

@Log
public enum Downloader {
    INSTANCE;
    public Document getDocument(String url) {
        log.info("getDocument: " + url);
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
