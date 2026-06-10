package com.example.online_bank.repository;

import com.example.online_bank.domain.entity.User;
import com.example.online_bank.domain.entity.UserCategoryStats;
import com.example.online_bank.enums.PartnerCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCategoryStatsRepository extends JpaRepository<UserCategoryStats, Long> {
    Optional<UserCategoryStats> findByUser_UuidAndCategoryAndLastSpendDateBetween(
            UUID userUuid,
            PartnerCategory category,
            LocalDate start,
            LocalDate end
    );

    List<UserCategoryStats> findAllByUser(User user);

    User user(User user);
}
