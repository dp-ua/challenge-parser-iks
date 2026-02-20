package com.dp_ua.iksparser.dba.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
    protected LocalDateTime created;

    protected LocalDateTime updated;

    @PrePersist
    protected void onCreate() {
        var now = LocalDateTime.now();
        created = now;
        updated = now;
        log.debug("created: {}, {}", this.getClass().getSimpleName(), this);
    }

    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
        log.debug("updated: {}, {}", this.getClass().getSimpleName(), this);
    }

}
