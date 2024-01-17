package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.element.Competition;
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

    public List<Competition> findAll() {
        List<Competition> competitions = new ArrayList<>();
        repo.findAll().forEach(competitions::add);
        return competitions;
    }

    public Competition saveOrUpdate(Competition competition) {
        Competition competitionFromDb = repo.findByNameAndBeginDateAndUrl(
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
}
