package com.dp_ua.iksparser.bot.abilities;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class StateService {
    private final Map<String, String> stateMap = new HashMap<>();
    private LocalDateTime updateCompetitionsTime;
    @Getter
    private BotSession botSession;

    public String getState(String chatId) {
        String entry = stateMap.get(chatId);
        stateMap.remove(chatId);
        return entry;
    }

    public void setState(String chatId, String state) {
        stateMap.put(chatId, state);
    }

    /**
     * Replaces placeholders `{}` in the template string with the provided arguments.
     *
     * @param template The template string containing `{}` placeholders.
     * @param args     Arguments to replace placeholders in the template string.
     * @return A string obtained after replacing placeholders with the provided arguments.
     */
    public static String formatArgs(String template, String... args) {
        String result = template;
        for (String arg : args) {
            result = result.replaceFirst("\\{\\}", arg);
        }
        return result;
    }

    public void saveBotSession(BotSession botSession) {
        this.botSession = botSession;
    }

    public LocalDateTime getUpdateCompetitionsTime() {
        return updateCompetitionsTime;
    }

    public void setUpdateCompetitionsTime(LocalDateTime now) {
        updateCompetitionsTime = now;
    }
}
