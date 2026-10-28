package com.webliix.careers.repository;

import com.webliix.careers.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findAllByOrderByCreatedAtDesc();

    List<JobApplication> findByStatusOrderByCreatedAtDesc(String status);

    List<JobApplication> findByJobTitleContainingIgnoreCaseOrderByCreatedAtDesc(String jobTitle);
}
