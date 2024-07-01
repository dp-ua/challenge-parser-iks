package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dp_ua.iksparser.bot.Icon.MARK;
import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

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

    public List<String> heatLinesListInfo(ParticipantEntity participant, List<HeatLineEntity> heatLines) {
        List<String> heatLinesInfo = heatLines.stream()
                .filter(heatLine -> !heatLine.getParticipant().equals(participant))
                .map(this::info)
                .toList();

        return SERVICE.removeDuplicates(heatLinesInfo);
    }
}
