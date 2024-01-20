package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.App;
import com.dp_ua.iksparser.dba.element.UpdateStatusEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class UpdateStatusRepoTest {
    @Autowired
    private UpdateStatusRepo repo;
    @MockBean
    App app;

    @Test
    public void shouldSaveAndRetrieveUpdateStatus() {
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