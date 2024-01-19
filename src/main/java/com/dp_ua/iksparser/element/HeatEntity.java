package com.dp_ua.iksparser.element;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@EqualsAndHashCode
public class HeatEntity extends DomainElement {
    private String name;
    @ManyToMany(mappedBy = "heats")
    private List<HeatLineEntity> heatLines;
}
