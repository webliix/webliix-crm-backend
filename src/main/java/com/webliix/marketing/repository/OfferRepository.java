package com.webliix.marketing.repository;

import com.webliix.marketing.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByActiveTrueOrderByCreatedAtDesc();
    List<Offer> findAllByOrderByCreatedAtDesc();
}
