package com.webliix.tickets.mapper;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.projects.entity.Project;
import com.webliix.tickets.dto.*;
import com.webliix.tickets.entity.Ticket;
import com.webliix.tickets.entity.TicketAttachment;
import com.webliix.tickets.entity.TicketComment;

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
        String customerName = null;
        Customer customer = ticket.getCustomer();
        if (customer != null) {
            customerName = customer.getCompanyName() != null ? customer.getCompanyName() : customer.getContactPerson();
        }

        String projectName = null;
        Project project = ticket.getProject();
        if (project != null) {
            projectName = project.getProjectName();
        }

        String assignedToName = null;
        Employee assigned = ticket.getAssignedTo();
        if (assigned != null) {
            assignedToName = assigned.getFirstName() + (assigned.getLastName() != null ? " " + assigned.getLastName() : "");
        }

        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .customerId(customer != null ? customer.getId() : null)
                .customerName(customerName)
                .projectId(project != null ? project.getId() : null)
                .projectName(projectName)
                .createdBy(ticket.getCreatedBy())
                .assignedToId(assigned != null ? assigned.getId() : null)
                .assignedToName(assignedToName)
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .category(ticket.getCategory())
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
                .ticketId(comment.getTicket().getId())
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
                .ticketId(attachment.getTicket().getId())
                .fileName(attachment.getFileName())
                .filePath(attachment.getFilePath())
                .fileType(attachment.getFileType())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
}
