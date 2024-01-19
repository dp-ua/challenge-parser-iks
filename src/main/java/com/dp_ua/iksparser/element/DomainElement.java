package com.dp_ua.iksparser.element;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class DomainElement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @Column(updatable = false)
    protected String created;

    protected String updated;

    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now().toString();
        updated = LocalDateTime.now().toString();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now().toString();
    }

    public LocalDateTime getUpdatedTime() {
        return LocalDateTime.parse(updated);
    }
}
