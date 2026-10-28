package com.webliix.careers.service;

import com.webliix.careers.dto.*;

import java.util.List;

public interface JobService {

    List<JobOpeningDto> getActiveOpenings();

    List<JobOpeningDto> getAllOpenings();

    JobOpeningDto getOpeningById(Long id);

    JobOpeningDto createOpening(CreateJobOpeningRequest request);

    JobOpeningDto updateOpening(Long id, CreateJobOpeningRequest request);

    void deleteOpening(Long id);

    JobApplicationDto submitApplication(SubmitJobApplicationRequest request);

    List<JobApplicationDto> getApplications(String status, String search);

    JobApplicationDto getApplicationById(Long id);

    JobApplicationDto updateApplicationStatus(Long id, UpdateJobApplicationStatusRequest request);

    void deleteApplication(Long id);
}
