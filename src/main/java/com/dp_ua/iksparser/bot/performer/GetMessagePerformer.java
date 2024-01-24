package com.dp_ua.iksparser.bot.performer;

import com.dp_ua.iksparser.bot.abilities.StateService;
import com.dp_ua.iksparser.bot.command.CommandInterface;
import com.dp_ua.iksparser.bot.command.TextCommandDetectorService;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.bot.message.SelfMessage;
import com.dp_ua.iksparser.bot.performer.event.GetMessageEvent;
import com.dp_ua.iksparser.exeption.NotForMeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class GetMessagePerformer implements ApplicationListener<GetMessageEvent> {
    @Autowired
    TextCommandDetectorService commandDetector;
    @Autowired
    StateService stateService;

    @Override
    public void onApplicationEvent(GetMessageEvent event) {
        Message message = event.getMessage();
        log.info("GetMessageEvent: {}", message);
        try {
            List<CommandInterface> commands = commandDetector.getParsedCommands(message.getMessageText());
            log.info("commands: {}", commands);
            if (commands.isEmpty()) {
                String state = stateService.getState(message.getChatId());
                if (state != null) {
                    log.info("Found state for chatId: {}, State: {}", message.getChatId(), state);
                    GetMessageEvent selfMessageEvent = getGetMessageEvent(state, message);
                    onApplicationEvent(selfMessageEvent);
                }
            } else {
                commands
                        .forEach(command -> command.execute(message));
            }
        } catch (NotForMeException e) {
            log.info("NotForMeException: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.info("IllegalArgumentException: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private GetMessageEvent getGetMessageEvent(String state, Message message) {
        String textCommand = StateService.formatArgs(state, message.getMessageText());
        SelfMessage selfMessage = new SelfMessage();
        selfMessage.setChatId(message.getChatId());
        selfMessage.setMessageText(textCommand);
        return new GetMessageEvent(message.getChatId(), selfMessage);
    }
}
