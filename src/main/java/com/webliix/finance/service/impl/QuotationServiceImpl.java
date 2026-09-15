package com.webliix.finance.service.impl;

import com.webliix.finance.dto.CreateQuotationRequest;
import com.webliix.finance.dto.QuotationResponse;
import com.webliix.finance.entity.Quotation;
import com.webliix.finance.mapper.QuotationMapper;
import com.webliix.finance.repository.QuotationRepository;
import com.webliix.finance.service.QuotationService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import com.webliix.crm.customer.repository.CustomerRepository;
import com.webliix.projects.repository.ProjectRepository;
import com.webliix.crm.customer.entity.Customer;
import com.webliix.projects.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;

@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final ProjectRepository projectRepository;

    @Override
    public QuotationResponse createQuotation(CreateQuotationRequest req) {
        Quotation q = QuotationMapper.toEntity(req);

        // set customer
        if (req.getCustomerId() != null) {
            Customer c = customerRepository.findById(req.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            q.setCustomer(c);
        }

        // set project
        if (req.getProjectId() != null) {
            Project p = projectRepository.findById(req.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            q.setProject(p);
        }

        // generate quotation number
        String prefix = "QT-" + Year.now().getValue() + "-";
        String next = "000001";
        var previous = quotationRepository.findTopByQuotationNumberStartingWithOrderByIdDesc(prefix);
        if (previous.isPresent()) {
            String prevNum = previous.get().getQuotationNumber();
            try {
                String seq = prevNum.substring(prevNum.lastIndexOf('-') + 1);
                long val = Long.parseLong(seq) + 1;
                next = String.format("%06d", val);
            } catch (Exception ignored) {
            }
        }
        q.setQuotationNumber(prefix + next);
        q.setCreatedAt(LocalDateTime.now());
        q.setUpdatedAt(LocalDateTime.now());

        var saved = quotationRepository.save(q);
        return QuotationMapper.toResponse(saved);
    }

    @Override
    public Page<QuotationResponse> getAllQuotations(Pageable pageable) {
        return quotationRepository.findAll(pageable).map(QuotationMapper::toResponse);
    }

    @Override
    public QuotationResponse getQuotation(Long id) {
        Quotation q = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
        return QuotationMapper.toResponse(q);
    }

    @Override
    public QuotationResponse updateQuotation(Long id, CreateQuotationRequest req) {
        Quotation q = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));
        // simple update: replace items and fields
        Quotation updated = QuotationMapper.toEntity(req);
        updated.setId(q.getId());
        updated.setQuotationNumber(q.getQuotationNumber());
        updated.setCustomer(q.getCustomer());
        updated.setProject(q.getProject());
        updated.setCreatedAt(q.getCreatedAt());
        updated.setConverted(q.getConverted());
        updated.setConvertedAt(q.getConvertedAt());
        updated.setUpdatedAt(LocalDateTime.now());
        var saved = quotationRepository.save(updated);
        return QuotationMapper.toResponse(saved);
    }

    @Override
    public void deleteQuotation(Long id) {
        if (!quotationRepository.existsById(id)) throw new ResourceNotFoundException("Quotation not found");
        quotationRepository.deleteById(id);
    }
}
