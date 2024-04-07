package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.dba.element.DayEntity;
import com.dp_ua.iksparser.dba.element.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.repo.CompetitionRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
@Transactional
public class CompetitionService {
    public static final String DD_MM_YYYY = "dd.MM.yyyy";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DD_MM_YYYY);
    private final CompetitionRepo repo;

    @Autowired
    public CompetitionService(CompetitionRepo repo) {
        this.repo = repo;
    }

    public List<CompetitionEntity> findAllOrderByBeginDateDesc() {
        List<CompetitionEntity> all = repo.findAll();
        all.sort((o1, o2) -> dateComparator.compare(o1.getBeginDate(), o2.getBeginDate()));
        Collections.reverse(all);
        return all;
    }

    private final Comparator<String> dateComparator = (o1, o2) -> {
        LocalDate date1 = LocalDate.parse(o1, FORMATTER);
        LocalDate date2 = LocalDate.parse(o2, FORMATTER);
        return date1.compareTo(date2);
    };

    public CompetitionEntity saveOrUpdate(CompetitionEntity competition) {
        CompetitionEntity competitionFromDb = repo.findByNameAndBeginDateAndUrl(
                competition.getName(),
                competition.getBeginDate(),
                competition.getUrl()
        );
        if (competitionFromDb == null) {
            return repo.save(competition);
        } else {
            competitionFromDb.fillCompetition(competition);
            repo.save(competitionFromDb);
        }
        return competitionFromDb;
    }

    public CompetitionEntity findById(long commandArgument) {
        return repo.findById(commandArgument).orElse(null);
    }

    public CompetitionEntity save(CompetitionEntity competition) {
        return repo.save(competition);
    }

    public CompetitionEntity findByName(String name) {
        return repo.findByName(name);
    }

    public void flush() {
        repo.flush();
    }

    public long count() {
        return repo.count();
    }

    public Page<CompetitionDto> getAllCompetitions(Pageable pageable) {
        Page<CompetitionEntity> entities = repo.findAllByOrderByBeginDateDesc(pageable);
        return entities.map(this::convertToDto);
    }

    private CompetitionDto convertToDto(CompetitionEntity competition) {
        CompetitionDto dto = new CompetitionDto();
        dto.setId(competition.getId());
        dto.setDays(competition.getDays().stream().map(DayEntity::getId).toList());
        dto.setName(competition.getName());
        dto.setStatus(competition.getStatus());
        dto.setBeginDate(competition.getBeginDate());
        dto.setEndDate(competition.getEndDate());
        dto.setCountry(competition.getCountry());
        dto.setCity(competition.getCity());
        dto.setUrl(competition.getUrl());
        return dto;
    }
}