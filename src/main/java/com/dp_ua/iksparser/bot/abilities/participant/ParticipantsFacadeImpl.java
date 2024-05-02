package com.dp_ua.iksparser.bot.abilities.participant;

import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.controller.BotController;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.HeatLineService;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import com.dp_ua.iksparser.dba.service.SubscriberService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ParticipantsFacadeImpl implements ParticipantFacade {
    @Autowired
    private ParticipantService participantService;
    @Autowired
    SubscribeFacade subscribeFacade;
    @Autowired
    ParticipantView participantView;

    @Override
    public void subscribe(String chatId, long commandArgument, Integer editMessageId) {
        log.info("SUBSCRIBE chatId: {}, commandArgument: {}", chatId, commandArgument);
        Optional<ParticipantEntity> optionalParticipant = participantService.findById(commandArgument);
        optionalParticipant.ifPresent(participant -> {
            subscribeFacade.subscribe(chatId, participant);
            subscribeFacade.inform(chatId, participant, editMessageId);
        });
    }

    @Override
    public void unsubscribe(String chatId, long commandArgument, Integer editMessageId) {
        log.info("UNSUBSCRIBE chatId: {}, commandArgument: {}", chatId, commandArgument);
        Optional<ParticipantEntity> optionalParticipant = participantService.findById(commandArgument);
        optionalParticipant.ifPresent(participant -> {
            subscribeFacade.unsubscribe(chatId, participant);
            subscribeFacade.inform(chatId, participant, editMessageId);
        });
    }

    @Override
    public String getInfoAboutParticipants() {
        Iterable<ParticipantEntity> participants = participantService.findAll();
        return participantView.participantsInfo(participants);
    }


    @Override
    public void showParticipants(String chatId, long commandArgument, Integer editMessageId) {
        log.info("SHOW PARTICIPANTS chatId: {}, commandArgument: {}", chatId, commandArgument);
        throw new UnsupportedOperationException("Not implemented yet");
    }


    @Autowired
    BotController bot;

    @Override
    @Transactional
    public void operateDuplicates() {
        List<ParticipantEntity> duplicates = participantService.findDuplicates();
        log.info("Found " + duplicates.size() + " duplicates");
        bot.sendMessageToAdmin("Found " + duplicates.size() + " duplicates");
        operateDuplicates(duplicates);
        bot.sendMessageToAdmin("All duplicates processed");

    }

    @Autowired
    SubscriberService subscribeService;
    @Autowired
    HeatLineService heatLineService;

    private void operateDuplicates(List<ParticipantEntity> duplicates) {
        int stop = 0;
        int breakCount = duplicates.size();
        log.info("Start duplicates processing. Duplicates count: " + duplicates.size());
        while (!duplicates.isEmpty()) {
            if (stop > breakCount) {
                log.error("Stop duplicates processing");
                break;
            }
            ParticipantEntity participant = duplicates.get(0);
            // create not immutable list
            List<ParticipantEntity> onePerson = duplicates.stream()
                    .filter(p -> p.getName().equals(participant.getName())
                            && p.getSurname().equals(participant.getSurname())
                            && p.getBorn().equals(participant.getBorn()))
                    .collect(Collectors.toList());

            if (onePerson.size() == 1) {
                duplicates.removeAll(onePerson);
                log.error("Found only one person: " + participant);
                continue;
            }
            // collect all heatLines
            List<HeatLineEntity> heatLines = new ArrayList<>();
            onePerson.stream()
                    .flatMap(p -> p.getHeatLines().stream())
                    .forEach(h -> {
                        if (!heatLines.contains(h)) {
                            heatLines.add(h);
                        }
                    });

            // collect all subscribers chats
            Set<String> chats = onePerson.stream()
                    .map(p -> subscribeService.findAllByParticipant(p))
                    .flatMap(List::stream)
                    .map(s -> s.getChatId())
                    .collect(Set::of, Set::add, Set::addAll);


            // get url from one person
            Optional<String> url = onePerson.stream()
                    .map(p -> p.getUrl())
                    .filter(u -> u != null) // get any url
                    .findFirst();

            // unsubscribe all chats
            onePerson.forEach(p -> {
                subscribeService.unsubscribeAll(p);
            });

            // set all heatLines to one participant
            participant.setHeatLines(heatLines);
            heatLines.stream().forEach(h -> {
                h.setParticipant(participant);
                heatLineService.save(h);
            });

            // set any url
            url.ifPresent(u -> participant.setUrl(u));
            // subscribe one participant to all chats
            chats.stream().forEach(chatId -> {
                subscribeFacade.subscribe(chatId, participant);
            });
            participantService.save(participant);
            log.info("Save participant: " + participant);

            onePerson.remove(participant);
            onePerson.forEach(p -> {
                participantService.delete(p);
                log.info("Delete participant: " + p);
                duplicates.remove(p);
            });
            duplicates.remove(participant);
            stop++;
        }
    }
}
