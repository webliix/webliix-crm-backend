package com.webliix.careers.service.impl;

import com.webliix.careers.dto.*;
import com.webliix.careers.entity.JobApplication;
import com.webliix.careers.entity.JobOpening;
import com.webliix.careers.repository.JobApplicationRepository;
import com.webliix.careers.repository.JobOpeningRepository;
import com.webliix.careers.service.JobService;
import com.webliix.crm.lead.dto.PublicLeadRequest;
import com.webliix.crm.lead.service.LeadService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobOpeningRepository jobOpeningRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final LeadService leadService;

    @Override
    @Transactional(readOnly = true)
    public List<JobOpeningDto> getActiveOpenings() {
        return jobOpeningRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toOpeningDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobOpeningDto> getAllOpenings() {
        return jobOpeningRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toOpeningDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobOpeningDto getOpeningById(Long id) {
        JobOpening opening = jobOpeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job opening not found with id: " + id));
        return toOpeningDto(opening);
    }

    @Override
    @Transactional
    public JobOpeningDto createOpening(CreateJobOpeningRequest request) {
        JobOpening opening = JobOpening.builder()
                .title(request.getTitle())
                .department(request.getDepartment())
                .type(request.getType())
                .location(request.getLocation())
                .experience(request.getExperience())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        return toOpeningDto(jobOpeningRepository.save(opening));
    }

    @Override
    @Transactional
    public JobOpeningDto updateOpening(Long id, CreateJobOpeningRequest request) {
        JobOpening opening = jobOpeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job opening not found with id: " + id));

        opening.setTitle(request.getTitle());
        opening.setDepartment(request.getDepartment());
        opening.setType(request.getType());
        opening.setLocation(request.getLocation());
        opening.setExperience(request.getExperience());
        opening.setDescription(request.getDescription());
        opening.setRequirements(request.getRequirements());
        if (request.getActive() != null) {
            opening.setActive(request.getActive());
        }

        return toOpeningDto(jobOpeningRepository.save(opening));
    }

    @Override
    @Transactional
    public void deleteOpening(Long id) {
        if (!jobOpeningRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job opening not found with id: " + id);
        }
        jobOpeningRepository.deleteById(id);
    }

    @Override
    @Transactional
    public JobApplicationDto submitApplication(SubmitJobApplicationRequest request) {
        JobApplication application = JobApplication.builder()
                .jobOpeningId(request.getJobOpeningId())
                .jobTitle(request.getJobTitle())
                .applicationType(StringUtils.hasText(request.getApplicationType()) ? request.getApplicationType() : "job")
                .name(request.getName())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone())
                .location(request.getLocation())
                .experience(request.getExperience())
                .portfolioUrl(request.getPortfolioUrl())
                .resumeLink(request.getResumeLink())
                .message(request.getMessage())
                .payoutMethod(request.getPayoutMethod())
                .payoutDetails(request.getPayoutDetails())
                .status("NEW")
                .build();

        JobApplication saved = jobApplicationRepository.save(application);

        // Also cross-sync as a Lead into CRM so it appears in standard multi-channel pipelines
        try {
            String appReq = String.format("[Career Application] [Role: %s] [Exp: %s] [Loc: %s] [Portfolio: %s] [Resume: %s] %s",
                    request.getJobTitle(),
                    request.getExperience() != null ? request.getExperience() : "N/A",
                    request.getLocation() != null ? request.getLocation() : "Remote",
                    request.getPortfolioUrl() != null ? request.getPortfolioUrl() : "None",
                    request.getResumeLink() != null ? request.getResumeLink() : "None",
                    request.getMessage() != null ? request.getMessage() : ""
            );

            PublicLeadRequest leadReq = PublicLeadRequest.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .companyName("Career Applicant")
                    .serviceRequested("Career Application: " + request.getJobTitle())
                    .requirements(appReq)
                    .source("CAREERS_PAGE")
                    .page("/careers")
                    .build();

            leadService.createPublicLead(leadReq);
        } catch (Exception ex) {
            log.warn("Non-fatal: could not cross-sync career application to leads table: {}", ex.getMessage());
        }

        return toApplicationDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobApplicationDto> getApplications(String status, String search) {
        List<JobApplication> list = jobApplicationRepository.findAllByOrderByCreatedAtDesc();

        return list.stream()
                .filter(a -> !StringUtils.hasText(status) || a.getStatus().equalsIgnoreCase(status.trim()))
                .filter(a -> {
                    if (!StringUtils.hasText(search)) return true;
                    String q = search.trim().toLowerCase();
                    return (a.getName() != null && a.getName().toLowerCase().contains(q))
                            || (a.getEmail() != null && a.getEmail().toLowerCase().contains(q))
                            || (a.getJobTitle() != null && a.getJobTitle().toLowerCase().contains(q))
                            || (a.getLocation() != null && a.getLocation().toLowerCase().contains(q));
                })
                .map(this::toApplicationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobApplicationDto getApplicationById(Long id) {
        JobApplication app = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found with id: " + id));
        return toApplicationDto(app);
    }

    @Override
    @Transactional
    public JobApplicationDto updateApplicationStatus(Long id, UpdateJobApplicationStatusRequest request) {
        JobApplication app = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found with id: " + id));

        app.setStatus(request.getStatus().toUpperCase());
        if (request.getNotes() != null) {
            app.setNotes(request.getNotes());
        }

        return toApplicationDto(jobApplicationRepository.save(app));
    }

    @Override
    @Transactional
    public void deleteApplication(Long id) {
        if (!jobApplicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job application not found with id: " + id);
        }
        jobApplicationRepository.deleteById(id);
    }

    private JobOpeningDto toOpeningDto(JobOpening o) {
        return JobOpeningDto.builder()
                .id(o.getId())
                .title(o.getTitle())
                .department(o.getDepartment())
                .type(o.getType())
                .location(o.getLocation())
                .experience(o.getExperience())
                .description(o.getDescription())
                .requirements(o.getRequirements())
                .active(o.getActive())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private JobApplicationDto toApplicationDto(JobApplication a) {
        return JobApplicationDto.builder()
                .id(a.getId())
                .jobOpeningId(a.getJobOpeningId())
                .jobTitle(a.getJobTitle())
                .applicationType(a.getApplicationType())
                .name(a.getName())
                .email(a.getEmail())
                .phone(a.getPhone())
                .location(a.getLocation())
                .experience(a.getExperience())
                .portfolioUrl(a.getPortfolioUrl())
                .resumeLink(a.getResumeLink())
                .message(a.getMessage())
                .payoutMethod(a.getPayoutMethod())
                .payoutDetails(a.getPayoutDetails())
                .status(a.getStatus())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
