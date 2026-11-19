package com.dp_ua.iksparser.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Configuration properties for bot views (page sizes).
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "view")
public class ViewProperties {

    private Participants participants = new Participants();

    private Competitions competitions = new Competitions();

    @Data
    public static class Participants {

        @Positive
        private int pageSize = 10;

    }

    @Data
    public static class Competitions {

        private Page general = new Page(5);

        private Page detailed = new Page(3);

    }

    @Data
    public static class Page {

        @Positive
        private int pageSize;

        public Page() {
        }

        public Page(int pageSize) {
            this.pageSize = pageSize;
        }

    }

}
