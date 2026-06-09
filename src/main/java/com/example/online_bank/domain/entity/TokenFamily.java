package com.example.online_bank.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
@Setter
public class TokenFamily {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "trusted_device_id", referencedColumnName = "id")
    private TrustedDevice trustedDevice;

    @OneToMany(mappedBy = "family")
    @ToString.Exclude
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

//    @ManyToOne
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
//    private User user;
}
