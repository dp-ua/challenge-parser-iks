package com.dp_ua.iksparser.bot.performer;

import com.dp_ua.iksparser.bot.performer.event.GetMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

//@Component
// not need now. Disabled
@Slf4j
public class StatisticService implements ApplicationListener<GetMessageEvent> {
    @Override
    public void onApplicationEvent(GetMessageEvent event) {
        // todo implement statistic or may be work on Aspects.
        // think about it
        log.info("GetMessageEvent: " + event.getMessage().getType());
    }
}
