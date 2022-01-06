package com.pharma.drugverification.repository;

import com.pharma.drugverification.domain.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrugRepository extends JpaRepository<Drug, Long> {

    Optional<Drug> findByCryptoIdentifier(String cryptoIdentifier);

    Optional<Drug> findByNdc(String ndc);

    Page<Drug> findByStatus(Drug.DrugStatus status, Pageable pageable);

    Page<Drug> findByManufacturerId(Long manufacturerId, Pageable pageable);

    List<Drug> findByStatusAndManufacturerId(Drug.DrugStatus status, Long manufacturerId);

    boolean existsByCryptoIdentifier(String cryptoIdentifier);

    boolean existsByNdc(String ndc);
}
