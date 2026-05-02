package com.example.online_bank.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BonusAccount {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column
    private BigDecimal points;

    @JoinColumn(name = "account_id")
    @OneToOne(cascade = ALL)
    private Account account;

    @JoinColumn(name = "user_id")
    @ManyToOne
    private User user;
}
