package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import org.springframework.stereotype.Component;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
public class ParticipantView {
    public String info(ParticipantEntity participant) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(ATHLETE)
                .append(participant.getSurname())
                .append(" ")
                .append(participant.getName());
        if (!participant.getUrl().isEmpty()) {
            sb
                    .insert(0, LINK);
            sb
                    .append(" ")
                    .append(LINK_END)
                    .append(LINK_SEPARATOR)
                    .append(participant.getUrl())
                    .append(LINK_SEPARATOR_END);
        }
        sb
                .append(" ")
                .append(participant.getBorn())
                .append(BIRTHDAY);
        sb
                .append(END_LINE)
                .append(AREA)
                .append(participant.getRegion())
                .append(", ")
                .append(participant.getTeam());
        return sb.toString();
    }
}
