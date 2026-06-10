package com.example.online_bank.repository;

import com.example.online_bank.domain.entity.User;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

    boolean existsUserByPhoneNumber(@NonNull String phoneNumber);

    @Modifying
    void deleteByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    @Query("select u from User u where u.isVerified = true ")
    List<User> findAllIsVerified();

    @Modifying
    @Query(nativeQuery = true, value = "truncate table user_bank cascade")
    @Transactional
    void deleteAllCascade();
}
