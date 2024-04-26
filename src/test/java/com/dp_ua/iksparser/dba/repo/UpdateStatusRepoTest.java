package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.App;
import com.dp_ua.iksparser.dba.entity.UpdateStatusEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

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
        assert allByCompetitionIdAndStatus.size() == 1;
        UpdateStatusEntity updateStatusFromDb = allByCompetitionIdAndStatus.get(0);
        assert updateStatusFromDb.getStatus().equals(expectedStatus);
        assert updateStatusFromDb.getCompetitionId() == expectedCompetitionId;
    }
}