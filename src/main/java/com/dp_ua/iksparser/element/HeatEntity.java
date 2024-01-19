package com.dp_ua.iksparser.element;

import jakarta.persistence.*;
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
    private String time;
    @OneToMany(mappedBy = "heat", cascade = CascadeType.ALL)
    private List<HeatLineEntity> heatLines;
    @ManyToOne
    @JoinColumn(name = "day_id")
    private DayEntity day;

}
