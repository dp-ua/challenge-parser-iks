package com.dp_ua.iksparser.bot.performer;

import com.dp_ua.iksparser.bot.abilities.StateService;
import com.dp_ua.iksparser.bot.command.CommandInterface;
import com.dp_ua.iksparser.bot.command.TextCommandDetectorService;
import com.dp_ua.iksparser.bot.event.GetMessageEvent;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.bot.message.SelfMessage;
import com.dp_ua.iksparser.exeption.NotForMeException;
import com.dp_ua.iksparser.service.MessageCreator;
import com.dp_ua.iksparser.service.SqlPreprocessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

@Component
@Slf4j
public class GetMessagePerformer implements ApplicationListener<GetMessageEvent> {
    @Autowired
    TextCommandDetectorService commandDetector;
    @Autowired
    StateService stateService;
    @Autowired
    SqlPreprocessorService sqlPreprocessorService;

    @Override
    public void onApplicationEvent(GetMessageEvent event) {
        Message message = event.getMessage();
        log.info("GetMessageEvent: {}", message);
        try {
            List<CommandInterface> commands = commandDetector.getParsedCommands(message.getMessageText());
            log.info("commands: {}", commands.stream().map(o -> o.getClass().getSimpleName()).toList());
            if (commands.isEmpty()) {
                String state = stateService.getState(message.getChatId());
                if (state != null) {
                    log.info("Found state for chatId: {}, State: {}", message.getChatId(), state);
                    GetMessageEvent selfMessageEvent = getGetMessageEvent(state, message);
                    onApplicationEvent(selfMessageEvent);
                }
            } else {
                stateService.resetState(message.getChatId());
                commands
                        .forEach(command -> command.execute(message));
            }
        } catch (NotForMeException e) {
            log.info("NotForMeException: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("IllegalArgumentException: {}", e);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private GetMessageEvent getGetMessageEvent(String state, Message message) {
        String text = SERVICE.cleanMarkdown(message.getMessageText());
        String textCommand = StateService.formatArgs(state, text);
        SelfMessage selfMessage = new SelfMessage();
        selfMessage.setChatId(message.getChatId());
        selfMessage.setMessageText(textCommand);
        return new GetMessageEvent(message.getChatId(), selfMessage);
    }
}
