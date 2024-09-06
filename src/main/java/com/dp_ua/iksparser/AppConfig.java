package com.dp_ua.iksparser;

import com.dp_ua.iksparser.bot.Bot;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseType;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@EnableAsync
@EnableScheduling
public class AppConfig {
    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.name}")
    private String botUserName;

    @Bean
    public Bot bot() {
        log.info("Bot name:[{}] Bot token:[{}...]", botUserName, botToken.substring(0, 10));
        return new Bot(botToken, botUserName);
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean("taskExecutor")
    public TaskExecutor defaultTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setThreadNamePrefix("DefaultExecutor-");
        return executor;
    }

    @Bean("updateExecutor")
    public TaskExecutor updateTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setThreadNamePrefix("UpdateExecutor-");
        executor.setConcurrencyLimit(2);
        return executor;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public Map<ResponseType, Class<? extends ResponseContentGenerator>> contentMap(List<ResponseContentGenerator> contentGenerators) {
        return contentGenerators.stream()
                .filter(content -> content.getClass().isAnnotationPresent(ResponseTypeMarker.class))
                .collect(Collectors.toMap(
                        content -> content.getClass().getAnnotation(ResponseTypeMarker.class).value(),
                        ResponseContentGenerator::getClass,
                        (existing, replacement) -> {
                            log.warn("Duplicate ResponseType: {}. Using the existing implementation: {}",
                                    existing.getSimpleName(), replacement.getSimpleName());
                            return existing;
                        }
                ));
    }
}
