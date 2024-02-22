package com.dp_ua.iksparser;

import com.dp_ua.iksparser.bot.abilities.StateService;
import com.dp_ua.iksparser.bot.command.CommandInterface;
import com.dp_ua.iksparser.bot.command.CommandProvider;
import com.dp_ua.iksparser.bot.controller.ControllerService;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.util.List;

@Component
@Slf4j
public class App implements ApplicationListener<ContextRefreshedEvent>, Ordered {
    @Autowired
    ControllerService botController;

    @Autowired
    StateService stateService;
    @Autowired
    CommandProvider commandProvider;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        connectBot();
        editBotMenu();
    }

    public void editBotMenu() {
        List<BotCommand> commands = commandProvider.getCommands()
                .stream()
                .filter(CommandInterface::isNeedToAddToMenu)
                .map(c -> new BotCommand(c.command(), c.description()))
                .toList();
        log.info("Edit bot menu: " + commands);
        SetMyCommands setCommandsMessage = SetMyCommands.builder()
                .commands(commands)
                .build();
        SendMessageEvent event = new SendMessageEvent(this, setCommandsMessage, SendMessageEvent.MsgType.SET_MY_COMMANDS);
        publisher.publishEvent(event);
    }

    public void connectBot() {
        BotSession botSession = botController.botConnect();
        stateService.saveBotSession(botSession);
        log.info("BotSession: " + botSession);
    }

    @Override
    public int getOrder() {
        return SpringApp.ORDER_FOR_APP_AND_BOT_STARTER;
    }
}
