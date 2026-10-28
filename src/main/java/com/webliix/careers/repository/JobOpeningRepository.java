package com.webliix.careers.repository;

import com.webliix.careers.entity.JobOpening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOpeningRepository extends JpaRepository<JobOpening, Long> {

    List<JobOpening> findByActiveTrueOrderByCreatedAtDesc();

    List<JobOpening> findAllByOrderByCreatedAtDesc();
}
