package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.element.CompetitionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompetitionService {
    private final CompetitionRepo repo;

    @Autowired
    public CompetitionService(CompetitionRepo repo) {
        this.repo = repo;
    }

    public List<CompetitionEntity> findAllOrderByUpdated() {
        return repo.findAllByOrderByUpdated();
    }

    public List<CompetitionEntity> findAllOrderByBeginDate(boolean reverse) {
        if (reverse) {
            return repo.findAllByOrderByBeginDateDesc();
        }
        return repo.findAllByOrderByBeginDate();
    }

    public CompetitionEntity getFreshestCompetition() {
        List<CompetitionEntity> competitions = findAllOrderByUpdated();
        if (competitions.isEmpty()) {
            return null;
        }
        return competitions.get(0);
    }

    public List<CompetitionEntity> findAll() {
        List<CompetitionEntity> competitions = new ArrayList<>();
        repo.findAll().forEach(competitions::add);
        return competitions;
    }

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
}