package com.dp_ua.iksparser.dba.service;

import static com.dp_ua.iksparser.config.Constants.FORMATTER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.dba.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.CompetitionStatus;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.repo.CompetitionRepo;
import com.dp_ua.iksparser.service.PageableService;
import com.dp_ua.iksparser.service.SqlPreprocessorService;
import com.dp_ua.iksparser.service.YearRange;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class CompetitionService {

    private final CompetitionRepo repo;
    private final SqlPreprocessorService sqlPreprocessorService;
    private final PageableService pageableService;
    private final DayService dayService;

    public List<CompetitionEntity> findAllOrderByUpdated() {
        return repo.findAllByOrderByUpdated();
    }

    public List<CompetitionEntity> findAllOrderByBeginDateDesc() {
        List<CompetitionEntity> all = repo.findAll();
        all.sort((o1, o2) -> dateComparator.compare(o1.getBeginDate(), o2.getBeginDate()));
        Collections.reverse(all);
        return all;
    }

    public List<CompetitionEntity> getFilledCompetitions() {
        return repo.findAllWithFilledDays();
    }

    public YearRange getMinAndMaxYear() {
        List<String> beginDates = repo.findAllBeginDates();

        List<Integer> years = beginDates.stream()
                .map(date -> {
                    try {
                        return LocalDate.parse(date, FORMATTER).getYear();
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid date format: " + date, e);
                    }
                })
                .filter(year -> year != null)
                .sorted()
                .toList();

        if (!years.isEmpty()) {
            return new YearRange(years.get(0).toString(), years.get(years.size() - 1).toString());
        }
        return new YearRange(null, null);
    }

    private final Comparator<String> dateComparator = (o1, o2) -> {
        LocalDate date1 = getParsedDate(o1);
        LocalDate date2 = getParsedDate(o2);
        return date1.compareTo(date2);
    };

    public LocalDate getParsedDate(String textDate) {
        return LocalDate.parse(textDate, FORMATTER);
    }

    public CompetitionEntity saveOrUpdate(CompetitionEntity competition) {
        Optional<CompetitionEntity> competitionFromDb = getCompetitionFromDb(competition);
        return competitionFromDb
                .map(competitionEntity -> {
                    competitionEntity.fillCompetition(competition);
                    return repo.save(competitionEntity);
                })
                .orElseGet(() -> repo.save(competition));
    }

    private Optional<CompetitionEntity> getCompetitionFromDb(CompetitionEntity competition) {
        List<CompetitionEntity> competitions = repo.findByNameAndBeginDateAndEndDate(
                competition.getName(),
                competition.getBeginDate(),
                competition.getEndDate()
        );
        if (competitions.size() == 1) {
            return Optional.of(competitions.get(0));
        }
        return competitions.stream()
                .filter(c -> !c.isURLEmpty() || c.isFilled())
                .findFirst();
    }

    public CompetitionEntity findById(long id) {
        return repo.findById(id).orElse(null);
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
        return result.map(this::toDTO);
    }

    public CompetitionDto toDTO(CompetitionEntity competition) {
        CompetitionDto dto = new CompetitionDto();
        dto.setId(competition.getId());
        dto.setDays(competition.getDays().stream().map(dayService::toDTO).toList());
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
        return Arrays.stream(CompetitionStatus.values()).map(CompetitionStatus::getName).toList();
    }

    public void delete(CompetitionEntity c) {
        repo.delete(c);
    }

    public Page<CompetitionEntity> findCompetitionsByParticipant(ParticipantEntity participant, int page, int limit) {
        return repo.findCompetitionsByParticipant(participant.getId(), pageableService.createPageRequest(page, limit));
    }

    public Optional<CompetitionEntity> getCompetitionByHeatLine(HeatLineEntity heatLineEntity) {
        return repo.findCompetitionByHeatLine(heatLineEntity.getId());
    }
}