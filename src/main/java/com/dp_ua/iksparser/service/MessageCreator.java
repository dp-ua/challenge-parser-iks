package com.dp_ua.iksparser.service;

import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public enum MessageCreator {
    SERVICE;
    public static String END_LINE = "\n";
    public static String BOLD = "*";
    public static String ITALIC = "_";
    public static String CODE = "`";
    public static String LINK = "[";
    public static String LINK_END = "]";
    public static String LINK_SEPARATOR = "(";
    public static String LINK_SEPARATOR_END = ")";

    public SendChatAction getChatAction(String chatId) {
        SendChatAction sendChatAction = new SendChatAction();
        sendChatAction.setChatId(chatId);
        sendChatAction.setAction(ActionType.TYPING);
        return sendChatAction;
    }

    public SendMessage getSendMessage(String chatId, String text, ReplyKeyboard replyMarkup, boolean enableMarkdown) {
        SendMessage.SendMessageBuilder builder = SendMessage.builder();

        SendMessage message = builder
                .chatId(chatId)
                .text(text)
                .replyMarkup(replyMarkup)
                .build();
        message.enableMarkdown(enableMarkdown);
        return message;
    }

    public SendMessage getSendMessage(String chatId, String text, ReplyKeyboard replyMarkup) {
        return getSendMessage(chatId, text, replyMarkup, false);
    }

    public SendMessage getSendMessage(String chatId, String text) {
        return getSendMessage(chatId, text, null);
    }

    public String cleanMarkdown(String input) {
        String cleanedText = input.replaceAll("[*_`\\[\\]]", "");
        return cleanedText;
    }
}