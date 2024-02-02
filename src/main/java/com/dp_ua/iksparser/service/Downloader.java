package com.dp_ua.iksparser.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class Downloader {
    public Document getDocument(String url) {
        log.debug("getDocument: " + url);
        try {
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
            return Jsoup.connect(decodedUrl).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
