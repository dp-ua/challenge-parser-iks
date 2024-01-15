package com.dp_ua.iksparser.bot.performer;

import com.dp_ua.iksparser.bot.command.TextCommandDetectorService;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.bot.performer.event.GetMessageEvent;
import com.dp_ua.iksparser.exeption.NotForMeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GetMessagePerformer implements ApplicationListener<GetMessageEvent> {
    @Autowired
    TextCommandDetectorService commandDetector;

    @Override
    public void onApplicationEvent(GetMessageEvent event) {
        Message message = event.getMessage();
        log.info("GetMessageEvent: {}", message);
        try {
            commandDetector.getParsedCommands(message.getMessageText())
                    .forEach(command -> command.execute(message));
        } catch (NotForMeException e) {
            log.info("NotForMeException: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.info("IllegalArgumentException: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
