package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.dto.CoachDto;
import com.dp_ua.iksparser.dba.entity.CoachEntity;
import com.dp_ua.iksparser.dba.repo.CoachRepo;
import com.dp_ua.iksparser.service.SqlPreprocessorService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Transactional
@Slf4j
public class CoachService {
    private final CoachRepo repo;

    @Autowired
    public CoachService(CoachRepo repo) {
        this.repo = repo;
    }

    @Autowired
    private SqlPreprocessorService sqlPreprocessorService;

    @Transactional
    public CoachEntity save(CoachEntity coach) {
        return repo.save(coach);
    }

    public CoachEntity findByName(String name) {
        List<CoachEntity> coaches = repo.findAllByName(name);
        if (coaches.isEmpty()) {
            return null;
        }
        if (coaches.size() > 1) {
            log.warn("Found {} coaches with name {}", coaches.size(), name);
        }
        return coaches.get(0);
    }

    public List<CoachEntity> searchByNamePartialMatch(String partialName) {
        return repo.findByNameContainingIgnoreCase(partialName);
    }

    public CoachEntity findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public CoachDto getCoachDto(Long id) {
        return convertToDto(findById(id));
    }

    public List<CoachDto> getCoachesDtoList(List<Long> ids) {
        return ids.stream()
                .map(this::getCoachDto)
                .filter(coachDto -> coachDto != null)
                .toList();
    }

    public CoachDto convertToDto(CoachEntity coach) {
        if (coach == null) {
            return null;
        }
        CoachDto coachDto = new CoachDto();
        coachDto.setId(coach.getId());
        coachDto.setName(coach.getName());
        return coachDto;
    }


    public List<CoachDto> getByNamePartialMatch(String name) {
        String namePart = sqlPreprocessorService.escapeSpecialCharacters(name);
        return searchByNamePartialMatch(namePart).stream()
                .map(this::convertToDto)
                .toList();
    }
}
