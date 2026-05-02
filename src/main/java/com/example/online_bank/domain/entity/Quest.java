package com.example.online_bank.domain.entity;

import com.example.online_bank.enums.PartnerCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Quest {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private PartnerCategory category;
    @Column
    private LocalDate dateOfExpiry;
    @Column
    private Integer pointReward;

    @OneToMany(fetch = LAZY, mappedBy = "quest")
    @ToString.Exclude
    @Builder.Default
    private List<UserQuest> userQuest = new ArrayList<>();

    @Column
    private Integer progress;

}
