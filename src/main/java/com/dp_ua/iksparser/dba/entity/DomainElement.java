package com.dp_ua.iksparser.dba.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Getter
@Setter
@Entity
@Slf4j
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class DomainElement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @Column(updatable = false)
    protected Date created;

    protected Date updated;

    @PrePersist
    protected void onCreate() {
        created = new Date();
        updated = new Date();
        log.debug("created: {}, {}", this.getClass().getSimpleName(), this);
    }

    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
        log.debug("updated: {}, {}", this.getClass().getSimpleName(), this);
    }

    protected String codeURL(String url) {
        return url.replaceAll(" ", "%20")
                .replaceAll("\\+", "%2B")
                .replaceAll("\\(", "%2F")
                .replaceAll("\\)", "%29");
    }
}
