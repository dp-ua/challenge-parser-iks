package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.dp_ua.iksparser.bot.Icon.MARK;
import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;

@Component
public class HeatLineView {
    @Autowired
    EventView eventView;

    public String info(HeatLineEntity heatLine) {
        StringBuilder sb = new StringBuilder();
        HeatEntity heat = heatLine.getHeat();

        sb
                .append(MARK)
                .append(" ");
        sb.append(eventView.info(heat.getEvent()));
        sb
                .append(", ")
                .append(heat.getName())
                .append(", д.")
                .append(heatLine.getLane())
                .append(",bib.")
                .append(heatLine.getBib());

        sb.append(END_LINE);
        return sb.toString();
    }
}
