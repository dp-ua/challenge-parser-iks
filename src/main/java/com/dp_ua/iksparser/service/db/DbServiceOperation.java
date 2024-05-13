package com.dp_ua.iksparser.service.db;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DbServiceOperation {

    @Autowired
    BotController bot;
    @Autowired
    SubscriberService subscribeService;
    @Autowired
    HeatLineService heatLineService;
    @Autowired
    ParticipantService participantService;

    @Transactional
    public void operateDuplicates() {
        List<ParticipantEntity> duplicates = participantService.findDuplicates();
        log.info("Found " + duplicates.size() + " duplicates");
        bot.sendMessageToAdmin("Found " + duplicates.size() + " duplicates");
        operateDuplicates(duplicates);
        bot.sendMessageToAdmin("All duplicates processed");

    }


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
                    .collect(Collectors.toSet());


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
                subscribeService.subscribe(chatId, participant);
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
