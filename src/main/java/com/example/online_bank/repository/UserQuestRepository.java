package com.example.online_bank.repository;

import com.example.online_bank.domain.entity.User;
import com.example.online_bank.domain.entity.UserQuest;
import com.example.online_bank.enums.PartnerCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserQuestRepository extends JpaRepository<UserQuest, Long> {
    Optional<UserQuest> findByUserAndQuest_CategoryAndQuest_DateOfExpiryIsAfter(
            User user,
            PartnerCategory category,
            LocalDate spendPeriod
    );

    List<UserQuest> findAllByUser_Uuid(UUID userUuid);
}
