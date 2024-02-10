package com.dp_ua.iksparser.service;

import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static com.dp_ua.iksparser.bot.event.SendMessageEvent.MsgType.ANSWER_CALLBACK_QUERY;

public enum MessageCreator {
    SERVICE;
    public static final String END_LINE = "\n";
    public static final String BOLD = "*";
    public static final String SPACE = " ";
    public static final String ITALIC = "_";
    public static final String CODE = "`";
    public static final String LINK = "[";
    public static final String LINK_END = "]";
    public static final String LINK_SEPARATOR = "(";
    public static final String LINK_SEPARATOR_END = ")";

    public SendChatAction getChatAction(String chatId) {
        SendChatAction sendChatAction = new SendChatAction();
        sendChatAction.setChatId(chatId);
        sendChatAction.setAction(ActionType.TYPING);
        return sendChatAction;
    }

    public DeleteMessage getDeleteMessage(String chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        return deleteMessage;
    }

    public EditMessageText getEditMessageText(String chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard, boolean enableMarkdown) {
        EditMessageText.EditMessageTextBuilder builder = EditMessageText.builder();

        EditMessageText message = builder
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboard)
                .build();
        message.enableMarkdown(enableMarkdown);
        return message;
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
        return input.replaceAll("[*_`\\[\\]]", "");
    }

    public InlineKeyboardButton getKeyboardButton(String text, String callbackData) {
        final InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public SendMessageEvent getAnswerCallbackQuery(String callBackQueryId, String message) {
        if (message == null) return null;
        if (callBackQueryId == null) return null;
        if (!message.isEmpty()) {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callBackQueryId);
            answerCallbackQuery.setText(message);
            return
                    new SendMessageEvent(this, answerCallbackQuery, ANSWER_CALLBACK_QUERY);
        }
        return null;
    }

    public String maskApostrof(String text) {
        return text.replaceAll("'", "’");
    }
}