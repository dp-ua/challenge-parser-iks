package com.dp_ua.iksparser.element;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
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
    @OneToMany(mappedBy = "heat", cascade = CascadeType.ALL)
    private List<HeatLineEntity> heatLines;
}
