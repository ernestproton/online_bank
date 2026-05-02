package com.example.online_bank.domain.entity;

import com.example.online_bank.enums.CurrencyCode;
import com.example.online_bank.enums.OperationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;


/**
 * Сущность операция - уникальный идентификатор, дата+время (равна времени создания операции)
 */
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
public class Operation {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column
    private LocalDateTime createdAt;

    @Column
    @Enumerated(STRING)
    private OperationType operationType;

    @Column
    private BigDecimal amount;

    @Column
    private String description;

    @Column
    @Enumerated(STRING)
    private CurrencyCode currencyCode;

    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @ManyToOne
    private Account account;
}
