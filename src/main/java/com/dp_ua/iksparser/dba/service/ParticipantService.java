package com.dp_ua.iksparser.dba.service;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.dba.dto.ParticipantDto;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.repo.ParticipantRepo;
import com.dp_ua.iksparser.service.PageableService;
import com.dp_ua.iksparser.service.SqlPreprocessorService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepo repo;
    private final SqlPreprocessorService sqlPreprocessorService;
    private final PageableService pageableService;

    @Transactional
    public ParticipantEntity save(ParticipantEntity participant) {
        return repo.save(participant);
    }

    public ParticipantEntity findParticipant(
            String surname,
            String name,
            String born
    ) {
        List<ParticipantEntity> participants = repo.findAllBySurnameAndNameAndBorn(
                surname,
                name,
                born
        );
        if (participants.isEmpty()) {
            return null;
        }
        if (participants.size() > 1) {
            log.warn("Found " + participants.size() + " participants with surname " + surname + " name " + name + " born " + born);
        }
        return participants.get(0);
    }

    public Page<ParticipantEntity> findAllBySurnameAndNameParts(List<String> parts, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<String> maskedLowerCaseParts = parts != null ? getMaskedLowerCaseParts(parts) : Collections.emptyList();

        if (maskedLowerCaseParts.isEmpty()) {
            return repo.findAll(pageable);
        }

        List<ParticipantEntity> content = findAllBySurnameAndNameParts(parts);
        Collator collator = Collator.getInstance(new Locale("uk", "UA"));
        content.sort(Comparator.comparing(ParticipantEntity::getSurname, collator).thenComparing(ParticipantEntity::getName, collator));
        return pageableService.getPage(content, pageable);
    }

    protected List<ParticipantEntity> findAllBySurnameAndNameParts(List<String> parts) {
        Set<ParticipantEntity> result = new HashSet<>();

        if (parts == null || parts.isEmpty()) {
            repo.findAll().forEach(result::add);
            return new ArrayList<>(result);
        } else {
            List<String> maskedLowerCaseParts = getMaskedLowerCaseParts(parts);

            for (String part : maskedLowerCaseParts) {
                result.addAll(repo.findByNameAndSurnameByPart(part));
            }
            return result.stream()
                    .filter(participant -> containsParts(participant, maskedLowerCaseParts))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    private List<String> getMaskedLowerCaseParts(List<String> parts) {
        List<String> result = List.copyOf(parts.stream()
                .map(part -> part.split(" "))
                .flatMap(Arrays::stream)
                .filter(part -> !part.isBlank())
                .map(String::toLowerCase)
                .map(sqlPreprocessorService::escapeSpecialCharacters)
                .collect(Collectors.toSet()));
        log.info("Masked parts: {}", result);
        return result;
    }

    private boolean containsParts(ParticipantEntity participant, List<String> parts) {
        for (String part : parts) {
            if (isNotContainsInNameOrSurname(participant, part)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNotContainsInNameOrSurname(ParticipantEntity participant, String part) {
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

    public ParticipantDto toDTO(ParticipantEntity participant) {
        ParticipantDto dto = new ParticipantDto();
        dto.setId(participant.getId());
        dto.setSurname(participant.getSurname());
        dto.setName(participant.getName());
        dto.setTeam(participant.getTeam());
        dto.setRegion(participant.getRegion());
        dto.setBorn(participant.getBorn());
        dto.setUrl(participant.getUrl());
        return dto;
    }

    public List<ParticipantEntity> findDuplicates() {
        return repo.findDuplicates();
    }

    public void delete(ParticipantEntity p) {
        repo.delete(p);
    }

    public long getCount() {
        return repo.count();
    }
}
