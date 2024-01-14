package com.dp_ua.iksparser;

import lombok.SneakyThrows;
import org.junit.Before;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public abstract class MockBotControllerTest {
    @Mock
    BotSession botSession;
    @MockBean
    TelegramBotsApi telegramBotsApi;

    @SneakyThrows
    @Before
    public void setUp() {
        when(telegramBotsApi.registerBot(any(LongPollingBot.class))).thenReturn(botSession);
        additionalSetUp();
    }

    public abstract void additionalSetUp();
}
