package com.dp_ua.iksparser.dba.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.dp_ua.iksparser.App;
import com.dp_ua.iksparser.dba.entity.UpdateStatusEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class UpdateStatusRepoTest {

    @Autowired
    private UpdateStatusRepo repo;
    @MockBean
    App app;

    @Test
    void shouldSaveAndRetrieveUpdateStatus() {
        // given
        String expectedStatus = "Test status";
        long expectedCompetitionId = 1L;

        // when
        UpdateStatusEntity updateStatus = new UpdateStatusEntity();
        updateStatus.setStatus(expectedStatus);
        updateStatus.setCompetitionId(expectedCompetitionId);
        repo.save(updateStatus);

        // then
        List<UpdateStatusEntity> allByCompetitionIdAndStatus = repo.findAllByCompetitionIdAndStatus(expectedCompetitionId, expectedStatus);
        assertEquals(1, allByCompetitionIdAndStatus.size());
        UpdateStatusEntity updateStatusFromDb = allByCompetitionIdAndStatus.get(0);
        assertEquals(expectedStatus, updateStatusFromDb.getStatus());
        assertEquals(expectedCompetitionId, updateStatusFromDb.getCompetitionId());
    }

}
