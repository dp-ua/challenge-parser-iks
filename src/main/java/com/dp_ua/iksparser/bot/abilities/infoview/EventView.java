package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.dba.element.DayEntity;
import com.dp_ua.iksparser.dba.element.EventEntity;

import static com.dp_ua.iksparser.bot.Icon.RESULT;
import static com.dp_ua.iksparser.service.MessageCreator.*;

public class EventView {
    public static String info(EventEntity event) {
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

    private static String getLink(EventEntity event) {
        return !event.getResultUrl().isEmpty() ? event.getResultUrl()
                :
                !event.getStartListUrl().isEmpty()
                        ?
                        event.getStartListUrl()
                        :
                        "";
    }
}
