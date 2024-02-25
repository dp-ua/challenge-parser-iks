package com.dp_ua.iksparser.bot.abilities;

import com.dp_ua.iksparser.bot.abilities.competition.CompetitionFacade;
import com.dp_ua.iksparser.bot.abilities.infoview.MenuView;
import com.dp_ua.iksparser.bot.abilities.participant.ParticipantFacade;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

@Component
@Slf4j
public class MainFacadeImpl implements MainFacade {
    @Autowired
    MenuView menuView;
    @Autowired
    CompetitionFacade competitionFacade;
    @Autowired
    SubscribeFacade subscribeFacade;
    @Autowired
    ParticipantFacade participantFacade;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    @Transactional
    public void menu(String chatId, String argument, Integer editMessageId) {
        log.info("Show menu for chatId: {}, argument:{}", chatId, argument);

        String competitionsInfo = competitionFacade.getInfoAboutCompetitions();
        String participantsInfo = participantFacade.getInfoAboutParticipants();
        String subscribeInfo = subscribeFacade.getInfoAboutSubscribes(chatId);
        String menuText = menuView.mainMenu(competitionsInfo, participantsInfo, subscribeInfo);
        InlineKeyboardMarkup buttons = menuView.menuButtons();

        SendMessageEvent sendMessageEvent = SERVICE.getSendMessageEvent(chatId, menuText, buttons, editMessageId);
        publisher.publishEvent(sendMessageEvent);
    }
}
