package com.example.online_bank.domain.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Класс пользователь с атрибутами: телефоном, фио, случайно сгенерированным UUID (UUID.randomUUID()).
 * Генерируется случайный пин-код.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "user_bank")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column
    private UUID uuid;

    @Column
    private String passwordHash;

    @Column()
    private Integer failedAttempts;

    @Column
    private Boolean isBlocked;

    @Column
    private LocalDateTime blockedExpiredAt;

    @Column(unique = true)
    @Email
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    @Column
    private String name;

    @Column
    private String surname;

    @Column
    private String patronymic;

    @Column
    private Boolean isVerified;

    @ToString.Exclude
    @OneToMany(mappedBy = "holder", cascade = REMOVE, orphanRemoval = true, fetch = LAZY)
    @Builder.Default
    private List<Account> accounts = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "user", fetch = LAZY, cascade = REMOVE)
    @Builder.Default
    private List<VerificationCode> verificationCode = new ArrayList<>();

    @ManyToMany()
    @ToString.Exclude
    @JoinTable(
            name = "role_user",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    @ToString.Exclude
    @OneToMany(mappedBy = "user", orphanRemoval = true, fetch = LAZY, cascade = REMOVE)

    @Builder.Default
    private List<TrustedDevice> trustedDevice = new ArrayList<>();

//    @ToString.Exclude
//    @OneToMany(mappedBy = "user", fetch = LAZY, cascade = REMOVE)
//    @Builder.Default
//    private List<TokenFamily> tokenFamilies = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "user", fetch = LAZY, cascade = REMOVE)
    @Builder.Default
    private List<UserCategoryStats> userCategoryStats = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(fetch = LAZY, mappedBy = "user", cascade = REMOVE)
    @Builder.Default
    private List<UserQuest> userQuest = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = REMOVE)
    @Builder.Default
    private List<DeviceChallenge> deviceChallenges = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = REMOVE)
    @ToString.Exclude
    @Builder.Default
    private List<BonusAccount> bonusAccounts = new ArrayList<>();
}
