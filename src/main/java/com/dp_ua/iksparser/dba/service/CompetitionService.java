package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.CompetitionStatus;
import com.dp_ua.iksparser.dba.entity.DayEntity;
import com.dp_ua.iksparser.dba.repo.CompetitionRepo;
import com.dp_ua.iksparser.service.PageableService;
import com.dp_ua.iksparser.service.SqlPreprocessorService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Transactional
public class CompetitionService {
    public static final String DD_MM_YYYY = "dd.MM.yyyy";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DD_MM_YYYY);
    private final CompetitionRepo repo;

    @Autowired
    SqlPreprocessorService sqlPreprocessorService;
    @Autowired
    PageableService pageableService;

    @Autowired
    public CompetitionService(CompetitionRepo repo) {
        this.repo = repo;
    }

    public List<CompetitionEntity> findAllOrderByUpdated() {
        return repo.findAllByOrderByUpdated();
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

    @Transactional
    public Page<CompetitionDto> getCompetitions(String text, String status, int page, int size) {
        List<CompetitionEntity> content = findAllOrderByBeginDateDesc();
        if (status != null && !status.isEmpty()) {
            content = getCompetitionFilteredByStatus(status, content);
        }
        if (text != null) {
            content = getCompetitionsFilteredByText(text, content);
        }
        return getCompetitionDtos(content, page, size);
    }

    private List<CompetitionEntity> getCompetitionFilteredByStatus(String status, List<CompetitionEntity> content) {
        List<String> filter = getParts(sqlPreprocessorService.escapeSpecialCharacters(status).toLowerCase());

        content = content.stream().filter(
                        competition ->
                                filter.stream().anyMatch(
                                        part ->
                                                competition.getStatus().toLowerCase().contains(part)
                                )
                )
                .toList();
        return content;
    }

    private List<CompetitionEntity> getCompetitionsFilteredByText(String text, List<CompetitionEntity> content) {
        List<String> parts = getParts(sqlPreprocessorService.escapeSpecialCharacters(text).toLowerCase());
        content = content.stream().filter(
                        competition ->
                                parts.stream().allMatch(
                                        part ->
                                                competition.getName().toLowerCase().contains(part)
                                )
                )
                .toList();
        return content;
    }

    private List<String> getParts(String text) {
        return Arrays.asList(text.split(" "));
    }

    private Page<CompetitionDto> getCompetitionDtos(List<CompetitionEntity> content, int page, int size) {
        Page<CompetitionEntity> result = pageableService.getPage(content, page, size);
        return result.map(this::convertToDto);
    }

    public CompetitionDto convertToDto(CompetitionEntity competition) {
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

    public Page<CompetitionEntity> getPagedCompetitionsClosetToDate(LocalDateTime date, int pageSize) {
        List<CompetitionEntity> content = findAllOrderByBeginDateDesc();
        int page = getPage(date, pageSize, content);
        return pageableService.getPage(content, page, pageSize);
    }

    public Page<CompetitionEntity> getPagedCompetitions(int page, int pageSize) {
        List<CompetitionEntity> content = findAllOrderByBeginDateDesc();
        return pageableService.getPage(content, page, pageSize);
    }

    private int getPage(LocalDateTime date, int pageSize, List<CompetitionEntity> content) {
        int page;

        Map<Long, Integer> compareMap = new HashMap<>();
        for (int i = 0; i < content.size(); i++) {
            CompetitionEntity competition = content.get(i);
            LocalDateTime competitionDate = LocalDateTime.parse(
                    String.format("%s %s",
                            competition.getBeginDate(), date.format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
            long hours = Math.abs(ChronoUnit.HOURS.between(date, competitionDate));
            compareMap.put(hours, i);
        }
        int index = compareMap.get(Collections.min(compareMap.keySet()));
        page = index / pageSize;
        return page;
    }

    public List<String> getAllStatuses() {
        return Arrays.asList(CompetitionStatus.values()).stream().map(CompetitionStatus::getName).toList();
    }
}