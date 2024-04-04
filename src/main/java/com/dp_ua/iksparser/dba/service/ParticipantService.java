package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.repo.ParticipantRepo;
import com.dp_ua.iksparser.service.SqlPreprocessorService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
@Slf4j
public class ParticipantService {
    private final ParticipantRepo repo;
    @Autowired
    private SqlPreprocessorService sqlPreprocessorService;

    @Autowired
    public ParticipantService(ParticipantRepo repo) {
        this.repo = repo;
    }

    @Transactional
    public ParticipantEntity save(ParticipantEntity participant) {
        return repo.save(participant);
    }

    public ParticipantEntity findBySurnameAndNameAndTeamAndRegionAndBorn(
            String surname,
            String name,
            String team,
            String region,
            String born
    ) {
        List<ParticipantEntity> participants = repo.findAllBySurnameAndNameAndTeamAndRegionAndBorn(
                surname,
                name,
                team,
                region,
                born
        );
        if (participants.isEmpty()) {
            return null;
        }
        if (participants.size() > 1) {
            log.warn("Found " + participants.size() + " participants with surname " + surname + " name " + name + " team " + team + " region " + region + " born " + born);
        }
        return participants.get(0);
    }

    public List<ParticipantEntity> findBySurnameAndNameParts(List<String> parts) {
        List<String> maskedLowerCaseParts = getMaskedLowerCaseParts(parts);

        Set<ParticipantEntity> result = new HashSet<>();
        for (String part : maskedLowerCaseParts) {
            result.addAll(repo.findByNameAndSurnameByPart(part));
        }
        return result.stream()
                .filter(participant -> containsParts(participant, maskedLowerCaseParts))
                .toList();
    }

    private List<String> getMaskedLowerCaseParts(List<String> parts) {
        return List.copyOf(parts.stream()
                .map(String::toLowerCase)
                .map(sqlPreprocessorService::escapeSpecialCharacters)
                .collect(Collectors.toSet()));
    }

    private boolean containsParts(ParticipantEntity participant, List<String> parts) {
        for (String part : parts) {
            if (isNotContainsInNameOrSurname(participant, part)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNotContainsInNameOrSurname(ParticipantEntity participant, String part) {
        return !(participant.getName().toLowerCase().contains(part)
                ||
                participant.getSurname().toLowerCase().contains(part));
    }

    public Optional<ParticipantEntity> findById(long commandArgument) {
        return repo.findById(commandArgument);
    }

    public Iterable<ParticipantEntity> findAll() {
        return repo.findAll();
    }
}
