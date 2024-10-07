package com.dp_ua.iksparser.bot.abilities.subscribe;

import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

import java.util.List;

public interface SubscribeFacade {
    boolean isSubscribed(String chatId, ParticipantEntity participant);

    void subscribe(String chatId, ParticipantEntity participant);

    void unsubscribe(String chatId);

    void unsubscribe(String chatId, ParticipantEntity participant);

    void inform(String chatId, ParticipantEntity participant, Integer editMessageId);

    void operateParticipantWithHeatlines(ParticipantEntity participant, List<HeatLineEntity> heatLines);

    void showSubscriptions(String chatId, long commandArgument, Integer editMessageId);

    void showSubscriptionsList(String chatId, int page, Integer editMessageId);

    String getInfoAboutSubscribes(String chatId);
}
