package com.example.online_bank.repository;

import com.example.online_bank.domain.entity.BonusAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BonusAccountRepository extends JpaRepository<BonusAccount, Long> {
    Optional<BonusAccount> findByAccount_AccountNumber(String accountNumber);

    List<BonusAccount> findAllByUser_Uuid(UUID uuid);
}
