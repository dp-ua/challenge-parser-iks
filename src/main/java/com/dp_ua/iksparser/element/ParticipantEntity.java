package com.dp_ua.iksparser.element;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
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
public class ParticipantEntity extends DomainElement {
    private String surname;
    private String name;
    private String team;
    private String city;
    private String born;
    private String url;
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private List<HeatLineEntity> heatLines;
}
