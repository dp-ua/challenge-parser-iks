package com.dp_ua.iksparser.bot.abilities.subscribe;

import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;

import java.util.List;

public interface SubscribeFacade {
    boolean isSubscribed(String chatId, Long id);

    void operateParticipantWithHeatlines(ParticipantEntity participant, List<HeatLineEntity> heatLines);

    void showSubscribers(String chatId, long commandArgument, Integer editMessageId);

    void unsubscribe(String chatId);

}
