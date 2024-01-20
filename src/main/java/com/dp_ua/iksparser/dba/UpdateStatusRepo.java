package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.dba.element.UpdateStatusEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpdateStatusRepo extends CrudRepository<UpdateStatusEntity, Long> {
}
