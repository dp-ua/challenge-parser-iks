package com.dp_ua.iksparser.service.db;

import com.dp_ua.iksparser.bot.controller.BotController;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.entity.SubscriberEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.dba.service.HeatLineService;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import com.dp_ua.iksparser.dba.service.SubscriberService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
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
    @Autowired
    CompetitionService competitionService;

    @Transactional
    public void operateCompetitionsDuplicates() {
        List<CompetitionEntity> competitions = competitionService.findAllOrderByBeginDateDesc();
        bot.sendMessageToAdmin("1.[competitions] Found " + competitions.size() + " competitions");
        List<CompetitionEntity> needToDelete = getDuplicatesOfCompetition(competitions);
        bot.sendMessageToAdmin("2.[competitions] Found " + needToDelete.size() + " competitions to delete");
        deleteDuplicatesCompetitions(needToDelete);
        bot.sendMessageToAdmin("3.[competitions] All duplicates processed");
    }

    private void deleteDuplicatesCompetitions(List<CompetitionEntity> needToDelete) {
        log.info("Start delete duplicates competitions. Size: " + needToDelete.size());
        needToDelete.forEach(c -> {
            competitionService.delete(c);
            log.info("Delete competition: " + c);
        });
    }

    private List<CompetitionEntity> getDuplicatesOfCompetition(List<CompetitionEntity> competitions) {
        List<CompetitionEntity> needToDelete = new ArrayList<>();
        while (competitions.size() > 1) {
            CompetitionEntity competition = competitions.get(0);
            log.info("Check competition: " + competition);
            List<CompetitionEntity> duplicates = competitions.stream()
                    .filter(c -> c.getName().equals(competition.getName())
                            && c.getBeginDate().equals(competition.getBeginDate())
                            && c.getEndDate().equals(competition.getEndDate()))
                    .toList();
            if (duplicates.size() == 1) {
                log.info("No duplicates for the competition: " + competition);
                competitions.remove(competition);
                continue;
            }
            duplicates.forEach(c ->
                    {
                        if (!c.isFilled() || c.isURLEmpty()) {
                            log.info("Need to delete. Found not filled competition: " + c);
                            needToDelete.add(c);
                        }
                        log.info("Left competition: " + c);
                        competitions.remove(c);
                    }
            );
        }
        return needToDelete;
    }

    @Transactional
    public void operateParticipantsDuplicates() {
        List<ParticipantEntity> duplicates = participantService.findDuplicates();
        log.info("1.[participants] Found " + duplicates.size() + " duplicates");
        bot.sendMessageToAdmin("1.[participants] Found " + duplicates.size() + " duplicates");
        operateParticipantsDuplicates(duplicates);
        bot.sendMessageToAdmin("2.[participants] All duplicates processed");
    }


    private void operateParticipantsDuplicates(List<ParticipantEntity> duplicates) {
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
                    .map(SubscriberEntity::getChatId)
                    .collect(Collectors.toSet());


            // get url from one person
            Optional<String> url = onePerson.stream()
                    .map(ParticipantEntity::getUrl)
                    .filter(Objects::nonNull) // get any url
                    .findFirst();

            // unsubscribe all chats
            onePerson.forEach(p -> subscribeService.unsubscribeAll(p));

            // set all heatLines to one participant
            participant.setHeatLines(heatLines);
            heatLines.forEach(h -> {
                h.setParticipant(participant);
                heatLineService.save(h);
            });

            // set any url
            url.ifPresent(participant::setUrl);
            // subscribe one participant to all chats
            chats.forEach(chatId -> subscribeService.subscribe(chatId, participant));
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
