package com.dp_ua.iksparser.bot.abilities.action;

import static com.dp_ua.iksparser.bot.command.CommandArgumentName.ACTION;
import static com.dp_ua.iksparser.bot.command.CommandArgumentName.PARTICIPANT_ID;

import java.util.Optional;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import com.dp_ua.iksparser.service.JsonReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActionController {
    private final JsonReader jsonReader;
    private final SubscribeFacade subscriptions;
    private final ParticipantService participantService;

    public void performAction(ActionType action, String userId, String text) {
        log.info("ActionController: action: {}, userId: {}, text: {}", action, userId, text);
        long participantId = getParticipantId(text);
        ParticipantEntity participant = participantService.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found by id: " + participantId));
        switch (action) { // todo make personal perfomers for actions, use Annotation, remove switch
            case SUB:
                subscriptions.subscribe(userId, participant);
                break;
            case UNS:
                subscriptions.unsubscribe(userId, participant);
                break;
            default:
                throw new IllegalArgumentException("Action not found: " + action);
        }
    }

    private long getParticipantId(String text) {
        return Long.parseLong(jsonReader.getVal(text, PARTICIPANT_ID.getValue()));
    }

    public Optional<ActionType> getActionType(String text) {
        try {
            String action = jsonReader.getVal(text, ACTION.getValue());
            log.debug("ActionController: action: {}, text: {}", action, text);
            return Optional.of(ActionType.valueOf(action));
        } catch (JSONException e) {
            log.debug("ActionController: No action found in text: {}", text);
            return Optional.empty();
        }
    }
}