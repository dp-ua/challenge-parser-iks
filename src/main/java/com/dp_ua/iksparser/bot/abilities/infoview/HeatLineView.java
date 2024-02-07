package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.dba.element.EventEntity;
import com.dp_ua.iksparser.dba.element.HeatEntity;
import com.dp_ua.iksparser.dba.element.HeatLineEntity;

import static com.dp_ua.iksparser.bot.Icon.MARK;
import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;

public class HeatLineView extends BaseView {
    public static String info(HeatLineEntity heatLine) {
        StringBuilder sb = new StringBuilder();
        HeatEntity heat = heatLine.getHeat();

        sb
                .append(MARK)
                .append(" ");
        sb.append(EventView.info(heat.getEvent()));
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
