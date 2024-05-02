package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.dto.ParticipantDto;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.repo.ParticipantRepo;
import com.dp_ua.iksparser.service.PageableService;
import com.dp_ua.iksparser.service.SqlPreprocessorService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
@Slf4j
public class ParticipantService {
    private final ParticipantRepo repo;
    @Autowired
    private SqlPreprocessorService sqlPreprocessorService;
    @Autowired
    PageableService pageableService;

    @Autowired
    public ParticipantService(ParticipantRepo repo) {
        this.repo = repo;
    }

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

    public Page<ParticipantEntity> findAllBySurnameAndNameParts(List<String> parts, Pageable pageable) {
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
            log.info("Masked parts: {}", maskedLowerCaseParts);

            for (String part : maskedLowerCaseParts) {
                result.addAll(repo.findByNameAndSurnameByPart(part));
            }
            return result.stream()
                    .filter(participant -> containsParts(participant, maskedLowerCaseParts))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    private List<String> getMaskedLowerCaseParts(List<String> parts) {
        return List.copyOf(parts.stream()
                .map(part -> part.split(" "))
                .flatMap(Arrays::stream)
                .filter(part -> !part.isBlank())
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

    public Page<ParticipantDto> getAll(Pageable pageable) {
        return repo.findAll(pageable)
                .map(this::convertToDto);
    }

    public ParticipantDto convertToDto(ParticipantEntity participant) {
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
}
