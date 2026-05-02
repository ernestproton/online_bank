package com.example.online_bank.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
@Setter
public class TrustedDevice {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column
    private String deviceName;
    @Column
    private String deviceId;
    @Column
    private String userAgent;
    @Column
    private LocalDateTime createdAt;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "trustedDevice")
    @ToString.Exclude
    @Builder.Default
    private List<TokenFamily> tokenFamilies = new ArrayList<>();
}
