package com.example.online_bank.repository;

import com.example.online_bank.domain.entity.ExchangeRate;
import com.example.online_bank.enums.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ExchangeCurrencyRepository extends JpaRepository<ExchangeRate, Long> {

    @Query("""
            select e.rate from ExchangeRate e
                        where e.baseCurrency = :baseCurrency
                                    and e.targetCurrency = :targetCurrency
            """)
    Optional<BigDecimal> findRateByBaseAndTargetCurrency(CurrencyCode baseCurrency, CurrencyCode targetCurrency);

    boolean existsByBaseCurrencyAndTargetCurrency(CurrencyCode selectedCurrencyCode, CurrencyCode accountCurrencyCode);
}
