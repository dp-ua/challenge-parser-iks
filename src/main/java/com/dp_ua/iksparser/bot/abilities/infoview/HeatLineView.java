package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.dba.element.DayEntity;
import com.dp_ua.iksparser.dba.element.EventEntity;
import com.dp_ua.iksparser.dba.element.HeatEntity;
import com.dp_ua.iksparser.dba.element.HeatLineEntity;

import static com.dp_ua.iksparser.bot.Icon.MARK;
import static com.dp_ua.iksparser.bot.Icon.RESULT;
import static com.dp_ua.iksparser.service.MessageCreator.*;

public class HeatLineView extends BaseView{
    public static String info(HeatLineEntity heatLine) {
        StringBuilder sb = new StringBuilder();
        HeatEntity heat = heatLine.getHeat();
        EventEntity event = heat.getEvent();
        DayEntity day = event.getDay();

        sb
                .append(MARK)
                .append(" ")
                .append(day.getDayName())
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
        sb

                .append(", ")
                .append(heat.getName())
                .append(", д.")
                .append(heatLine.getLane())
                .append(",bib.")
                .append(heatLine.getBib());
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
        sb.append(END_LINE);
        return sb.toString();
    }
}
