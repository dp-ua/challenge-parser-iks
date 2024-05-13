package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import org.springframework.stereotype.Component;

import static com.dp_ua.iksparser.bot.Icon.RESULT;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
public class EventView {
    public String info(EventEntity event) {
        DayEntity day = event.getDay();
        StringBuilder sb = new StringBuilder();
        sb.append(day.getDayName())
                .append(", ")
                .append(event.getTime())
                .append(", ");
        String link = getLink(event);
        if (!link.isEmpty()) {
            sb
                    .append(LINK)
                    .append(event.getEventName())
                    .append(", ")
                    .append(event.getCategory())
                    .append(", ")
                    .append(event.getRound())
                    .append(LINK_END)
                    .append(LINK_SEPARATOR)
                    .append(link)
                    .append(LINK_SEPARATOR_END);
        } else {
            sb.append(event.getEventName())
                    .append(", ")
                    .append(event.getRound());
        }
        if (!event.getResultUrl().isEmpty()) {
            sb
                    .append(" ")
                    .append(LINK)
                    .append(RESULT)
                    .append(LINK_END)
                    .append(LINK_SEPARATOR)
                    .append(event.getResultUrl())
                    .append(LINK_SEPARATOR_END);
        }
        return sb.toString();
    }

    private String getLink(EventEntity event) {
        return event.hasResultUrl() ? event.getResultUrl()
                :
                event.hasStartListUrl()
                        ?
                        event.getStartListUrl()
                        :
                        "";
    }
}
