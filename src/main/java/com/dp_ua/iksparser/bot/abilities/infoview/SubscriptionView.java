package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;

import java.util.List;

import static com.dp_ua.iksparser.bot.Icon.SUBSCRIBE;
import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;

public class SubscriptionView {
    public static String info(ParticipantEntity participant, List<HeatLineEntity> heatLines, CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder()
                .append(SUBSCRIBE)
                .append("Ви підписані на учасника: ")
                .append(END_LINE)
                .append(ParticipantView.info(participant))
                .append(END_LINE).append("Приймає участь у змаганнях: ")
                .append(CompetitionView.nameAndDate(competition))
                .append(END_LINE)
                .append("Нова заявка в забігах:")
                .append(END_LINE);
        for (HeatLineEntity heatLine : heatLines) {
            sb
                    .append(HeatLineView.info(heatLine))
                    .append(END_LINE);
        }
        return sb.toString();
    }
}
