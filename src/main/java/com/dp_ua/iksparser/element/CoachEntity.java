package com.dp_ua.iksparser.element;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class CoachEntity extends DomainElement {
    private String name;
    @ManyToMany(mappedBy = "coaches")
    private List<HeatLineEntity> heatLines;
}
