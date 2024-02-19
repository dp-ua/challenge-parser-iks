package com.dp_ua.iksparser.service;

import com.dp_ua.iksparser.exeption.ParsingException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static com.dp_ua.iksparser.exeption.ExceptionType.EMPTY_URL;

@Slf4j
@Component
public class Downloader {
    public Document getDocument(String url) throws ParsingException {
        log.debug("getDocument: " + url);
        if (url.isEmpty()) throw new ParsingException("Url is empty", EMPTY_URL);
        try {
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
            return Jsoup.connect(decodedUrl).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
