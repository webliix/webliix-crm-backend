package com.webliix.tickets.mapper;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.projects.entity.Project;
import com.webliix.tickets.dto.*;
import com.webliix.tickets.entity.Ticket;
import com.webliix.tickets.entity.TicketAttachment;
import com.webliix.tickets.entity.TicketComment;
import com.webliix.tickets.enums.TicketCategory;
import com.webliix.tickets.enums.TicketPriority;
import com.webliix.tickets.enums.TicketStatus;

public class TicketMapper {

    public static Ticket toEntity(CreateTicketRequest request) {
        return Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .createdBy(request.getCreatedBy())
                .priority(request.getPriority())
                .status(request.getStatus())
                .category(request.getCategory())
                .dueDate(request.getDueDate())
                .slaHours(request.getSlaHours())
                .responseTime(request.getResponseTime())
                .resolutionTime(request.getResolutionTime())
                .build();
    }

    public static void updateEntity(Ticket ticket, UpdateTicketRequest request) {
        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getCreatedBy() != null) {
            ticket.setCreatedBy(request.getCreatedBy());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
        }
        if (request.getCategory() != null) {
            ticket.setCategory(request.getCategory());
        }
        if (request.getDueDate() != null) {
            ticket.setDueDate(request.getDueDate());
        }
        if (request.getSlaHours() != null) {
            ticket.setSlaHours(request.getSlaHours());
        }
        if (request.getResponseTime() != null) {
            ticket.setResponseTime(request.getResponseTime());
        }
        if (request.getResolutionTime() != null) {
            ticket.setResolutionTime(request.getResolutionTime());
        }
    }

    public static TicketResponse toResponse(Ticket ticket) {
        if (ticket == null) return null;

        String customerName = null;
        Long customerId = null;
        try {
            Customer customer = ticket.getCustomer();
            if (customer != null) {
                customerId = customer.getId();
                customerName = customer.getCompanyName() != null ? customer.getCompanyName() : customer.getContactPerson();
            }
        } catch (Exception ignored) {}

        String projectName = null;
        Long projectId = null;
        try {
            Project project = ticket.getProject();
            if (project != null) {
                projectId = project.getId();
                projectName = project.getProjectName();
            }
        } catch (Exception ignored) {}

        String assignedToName = null;
        Long assignedToId = null;
        try {
            Employee assigned = ticket.getAssignedTo();
            if (assigned != null) {
                assignedToId = assigned.getId();
                assignedToName = assigned.getFirstName() + (assigned.getLastName() != null ? " " + assigned.getLastName() : "");
            }
        } catch (Exception ignored) {}

        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .customerId(customerId)
                .customerName(customerName)
                .projectId(projectId)
                .projectName(projectName)
                .createdBy(ticket.getCreatedBy())
                .assignedToId(assignedToId)
                .assignedToName(assignedToName)
                .priority(ticket.getPriority() != null ? ticket.getPriority() : TicketPriority.MEDIUM)
                .status(ticket.getStatus() != null ? ticket.getStatus() : TicketStatus.OPEN)
                .category(ticket.getCategory() != null ? ticket.getCategory() : TicketCategory.SUPPORT)
                .dueDate(ticket.getDueDate())
                .closedAt(ticket.getClosedAt())
                .slaHours(ticket.getSlaHours())
                .responseTime(ticket.getResponseTime())
                .resolutionTime(ticket.getResolutionTime())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    public static TicketComment toEntity(CreateTicketCommentRequest request) {
        return TicketComment.builder()
                .comment(request.getComment())
                .commentedBy(request.getCommentedBy())
                .build();
    }

    public static TicketCommentResponse toResponse(TicketComment comment) {
        return TicketCommentResponse.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket() != null ? comment.getTicket().getId() : null)
                .comment(comment.getComment())
                .commentedBy(comment.getCommentedBy())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public static TicketAttachment toEntity(CreateTicketAttachmentRequest request) {
        return TicketAttachment.builder()
                .fileName(request.getFileName())
                .filePath(request.getFilePath())
                .fileType(request.getFileType())
                .build();
    }

    public static TicketAttachmentResponse toResponse(TicketAttachment attachment) {
        return TicketAttachmentResponse.builder()
                .id(attachment.getId())
                .ticketId(attachment.getTicket() != null ? attachment.getTicket().getId() : null)
                .fileName(attachment.getFileName())
                .filePath(attachment.getFilePath())
                .fileType(attachment.getFileType())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
}
