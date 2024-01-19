package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.element.ParticipantEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantRepo extends CrudRepository<ParticipantEntity, Long> {
}
