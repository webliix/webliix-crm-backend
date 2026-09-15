package com.webliix.tickets.service;

import com.webliix.tickets.dto.*;

import java.util.List;

public interface TicketService {

    TicketResponse createTicket(CreateTicketRequest request);

    TicketResponse createPublicTicket(PublicTicketRequest request);

    List<TicketResponse> getAllTickets();

    TicketResponse getTicket(Long id);

    PublicTicketDetailsResponse getPublicTicket(String ticketNumber);

    TicketResponse updateTicket(Long id, UpdateTicketRequest request);

    TicketResponse assignTicket(Long id, AssignTicketRequest request);

    TicketCommentResponse addComment(Long id, CreateTicketCommentRequest request);

    TicketCommentResponse addPublicComment(String ticketNumber, CreateTicketCommentRequest request);

    List<TicketCommentResponse> getTicketComments(Long ticketId);

    TicketAttachmentResponse addAttachment(Long id, CreateTicketAttachmentRequest request);

    TicketDashboardResponse getDashboard();
}
