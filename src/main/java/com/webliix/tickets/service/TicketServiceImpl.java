package com.webliix.tickets.service;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.crm.customer.repository.CustomerRepository;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.projects.entity.Project;
import com.webliix.projects.repository.ProjectRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import com.webliix.tickets.dto.*;
import com.webliix.tickets.entity.Ticket;
import com.webliix.tickets.entity.TicketAttachment;
import com.webliix.tickets.entity.TicketComment;
import com.webliix.tickets.enums.TicketCategory;
import com.webliix.tickets.enums.TicketPriority;
import com.webliix.tickets.enums.TicketStatus;
import com.webliix.tickets.mapper.TicketMapper;
import com.webliix.tickets.repository.TicketAttachmentRepository;
import com.webliix.tickets.repository.TicketCommentRepository;
import com.webliix.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final CustomerRepository customerRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final TicketNumberGenerator ticketNumberGenerator;

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = TicketMapper.toEntity(request);
        ticket.setTicketNumber(ticketNumberGenerator.generateNextTicketNumber());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setSlaHours(resolveSlaHours(request.getSlaHours(), ticket.getPriority()));
        ticket.setStatus(ticket.getStatus() != null ? ticket.getStatus() : TicketStatus.OPEN);
        ticket.setPriority(ticket.getPriority() != null ? ticket.getPriority() : TicketPriority.MEDIUM);
        ticket.setCategory(ticket.getCategory() != null ? ticket.getCategory() : TicketCategory.SUPPORT);

        if (request.getCustomerId() != null) {
            ticket.setCustomer(customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId())));
        }
        if (request.getProjectId() != null) {
            ticket.setProject(projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId())));
        }
        if (request.getAssignedToId() != null) {
            ticket.setAssignedTo(employeeRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getAssignedToId())));
        }

        Ticket saved = ticketRepository.save(ticket);
        return TicketMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TicketResponse createPublicTicket(PublicTicketRequest request) {
        // Link or create customer record
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    Customer newCustomer = Customer.builder()
                            .companyName(request.getName())
                            .contactPerson(request.getName())
                            .email(request.getEmail())
                            .phone(request.getPhone())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return customerRepository.save(newCustomer);
                });

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumberGenerator.generateNextTicketNumber())
                .title(request.getTitle())
                .description(request.getDescription())
                .customer(customer)
                .createdBy(request.getName() + " (" + request.getEmail() + ")")
                .priority(TicketPriority.MEDIUM)
                .status(TicketStatus.OPEN)
                .category(request.getCategory() != null ? request.getCategory() : TicketCategory.SUPPORT)
                .slaHours(48)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        // Store initial question as first comment in chat thread
        TicketComment initialComment = TicketComment.builder()
                .ticket(savedTicket)
                .comment(request.getDescription())
                .commentedBy(request.getName())
                .createdAt(LocalDateTime.now())
                .build();
        commentRepository.save(initialComment);

        return TicketMapper.toResponse(savedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(TicketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long id) {
        return TicketMapper.toResponse(findTicketById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PublicTicketDetailsResponse getPublicTicket(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));

        List<TicketCommentResponse> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId())
                .stream()
                .map(TicketMapper::toResponse)
                .collect(Collectors.toList());

        String customerName = ticket.getCustomer() != null
                ? (ticket.getCustomer().getCompanyName() != null ? ticket.getCustomer().getCompanyName() : ticket.getCustomer().getContactPerson())
                : null;
        String customerEmail = ticket.getCustomer() != null ? ticket.getCustomer().getEmail() : null;

        return PublicTicketDetailsResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .customerName(customerName)
                .customerEmail(customerEmail)
                .createdBy(ticket.getCreatedBy())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .category(ticket.getCategory())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .comments(comments)
                .build();
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = findTicketById(id);
        TicketMapper.updateEntity(ticket, request);

        if (request.getCustomerId() != null) {
            ticket.setCustomer(customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId())));
        }
        if (request.getProjectId() != null) {
            ticket.setProject(projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId())));
        }
        if (request.getAssignedToId() != null) {
            ticket.setAssignedTo(employeeRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getAssignedToId())));
        }
        if (request.getPriority() != null) {
            ticket.setSlaHours(resolveSlaHours(request.getSlaHours(), ticket.getPriority()));
        }
        updateClosedAt(ticket);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updated = ticketRepository.save(ticket);
        return TicketMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public TicketResponse assignTicket(Long id, AssignTicketRequest request) {
        Ticket ticket = findTicketById(id);
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));
        ticket.setAssignedTo(employee);
        ticket.setUpdatedAt(LocalDateTime.now());
        return TicketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketCommentResponse addComment(Long id, CreateTicketCommentRequest request) {
        Ticket ticket = findTicketById(id);
        TicketComment comment = TicketMapper.toEntity(request);
        comment.setTicket(ticket);
        comment.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        TicketComment saved = commentRepository.save(comment);
        return TicketMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TicketCommentResponse addPublicComment(String ticketNumber, CreateTicketCommentRequest request) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));

        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
            ticket.setStatus(TicketStatus.REOPENED);
            ticket.setClosedAt(null);
        }
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        TicketComment comment = TicketMapper.toEntity(request);
        comment.setTicket(ticket);
        comment.setCreatedAt(LocalDateTime.now());
        TicketComment saved = commentRepository.save(comment);

        return TicketMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketCommentResponse> getTicketComments(Long ticketId) {
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(TicketMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TicketAttachmentResponse addAttachment(Long id, CreateTicketAttachmentRequest request) {
        Ticket ticket = findTicketById(id);
        TicketAttachment attachment = TicketMapper.toEntity(request);
        attachment.setTicket(ticket);
        attachment.setUploadedAt(LocalDateTime.now());
        TicketAttachment saved = attachmentRepository.save(attachment);
        return TicketMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDashboardResponse getDashboard() {
        TicketDashboardResponse response = new TicketDashboardResponse();
        response.setOpenTickets(ticketRepository.countByStatus(TicketStatus.OPEN));
        response.setInProgressTickets(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS));
        response.setResolvedTickets(ticketRepository.countByStatus(TicketStatus.RESOLVED));
        response.setCriticalTickets(ticketRepository.countByPriority(TicketPriority.CRITICAL));
        response.setOverdueTickets(ticketRepository.countByDueDateBeforeAndStatusNot(LocalDate.now(), TicketStatus.CLOSED));
        return response;
    }

    private Ticket findTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    private Integer resolveSlaHours(Integer slaHours, TicketPriority priority) {
        if (slaHours != null) {
            return slaHours;
        }
        if (priority == null) {
            return 48;
        }
        return switch (priority) {
            case LOW -> 72;
            case MEDIUM -> 48;
            case HIGH -> 24;
            case CRITICAL -> 4;
        };
    }

    private void updateClosedAt(Ticket ticket) {
        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
            if (ticket.getClosedAt() == null) {
                ticket.setClosedAt(LocalDateTime.now());
            }
        } else if (ticket.getStatus() == TicketStatus.REOPENED) {
            ticket.setClosedAt(null);
        }
    }
}
