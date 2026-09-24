package com.dp_ua.iksparser.dba.spec;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ParticipantSpecifications {

    private static final String FILED_NAME = "name";
    private static final String FIELD_SURNAME = "surname";

    public static Specification<ParticipantEntity> hasNameAndSurnameParts(List<String> parts) {
        return (root, query, cb) -> {
            if (parts == null || parts.isEmpty()) {
                return cb.conjunction();
            }

            var fullName = cb.concat(
                    cb.concat(root.get(FILED_NAME), " "),
                    root.get(FIELD_SURNAME)
            );

            var lowerFullName = cb.lower(fullName);

            var predicates = new ArrayList<Predicate>();

            parts.stream()
                    .filter(StringUtils::isNotBlank)
                    .forEach(part -> {
                        String pattern = "%" + part.toLowerCase() + "%";
                        predicates.add(cb.like(lowerFullName, pattern));
                    });

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
