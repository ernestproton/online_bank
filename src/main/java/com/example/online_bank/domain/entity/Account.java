package com.example.online_bank.domain.entity;

import com.example.online_bank.enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Номер счета (уникален), владелец (класс Пользователь этап1 пункт3), остаток на счете (с копейками).
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Getter
@Setter
public class Account {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column
    private BigDecimal balance;

    @Column
    private String accountNumber;

    @Column
    @Enumerated(STRING)
    private CurrencyCode currencyCode;

    @OneToMany(mappedBy = "account", fetch = LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<Operation> operations = new ArrayList<>();

    @Column()
    private Boolean isBlocked;

    @ManyToOne()
    @JoinColumn(name = "holder_id", referencedColumnName = "id")
    @ToString.Exclude
    private User holder;

    @OneToOne(mappedBy = "account")
    @ToString.Exclude
    private BankPartner bankPartner;

    @OneToOne(mappedBy = "account", cascade = MERGE)
    @ToString.Exclude
    private BonusAccount bonusAccount;
}
