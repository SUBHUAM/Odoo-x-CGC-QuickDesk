package com.odoo.quick_desk_backend.service;

import com.odoo.quick_desk_backend.entity.Ticket;
import com.odoo.quick_desk_backend.entity.User;
import com.odoo.quick_desk_backend.utility.TicketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    public void sendTicketStatusChangeEmail(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus) {
        if (!emailEnabled) {
            logger.info("Email notifications are disabled");
            return;
        }

        try {
            String to = ticket.getCreatedBy().getEmail();
            String subject = "Ticket Status Updated - #" + ticket.getTicketId();
            String body = String.format("""
                Dear %s,

                Your support ticket status has been updated.

                Ticket ID: #%d
                Subject: %s
                Status Changed: %s → %s
                Updated: %s

                %s

                Thank you for using our support system.

                Best regards,
                Quick Desk Support Team
                """,
                    ticket.getCreatedBy().getName(),
                    ticket.getTicketId(),
                    ticket.getSubject(),
                    oldStatus.name(),
                    newStatus.name(),
                    ticket.getUpdateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")),
                    ticket.getAssignedTo() != null ?
                            "Assigned Agent: " + ticket.getAssignedTo().getName() : "Not yet assigned to an agent"
            );

            sendSimpleEmail(to, subject, body);
            logger.info("Status change email sent for ticket: {} to: {}", ticket.getTicketId(), to);

        } catch (Exception e) {
            logger.error("Failed to send status change email for ticket: {}", ticket.getTicketId(), e);
        }
    }

    public void sendTicketAssignmentEmail(Ticket ticket, User assignedAgent) {
        if (!emailEnabled) return;

        try {
            // Email to ticket creator
            String creatorEmail = ticket.getCreatedBy().getEmail();
            String creatorSubject = "Your Ticket Has Been Assigned - #" + ticket.getTicketId();
            String creatorBody = String.format("""
                Dear %s,

                Good news! Your support ticket has been assigned to our support team.

                Ticket ID: #%d
                Subject: %s
                Assigned Agent: %s
                Agent Email: %s

                The assigned agent will begin working on your ticket and will contact you if additional information is needed.

                Thank you for your patience!

                Best regards,
                Quick Desk Support Team
                """,
                    ticket.getCreatedBy().getName(),
                    ticket.getTicketId(),
                    ticket.getSubject(),
                    assignedAgent.getName(),
                    assignedAgent.getEmail()
            );
            sendSimpleEmail(creatorEmail, creatorSubject, creatorBody);

            // Email to assigned agent
            String agentEmail = assignedAgent.getEmail();
            String agentSubject = "New Ticket Assigned - #" + ticket.getTicketId();
            String agentBody = String.format("""
                Dear %s,

                A new support ticket has been assigned to you.

                Ticket ID: #%d
                Subject: %s
                Category: %s
                Customer: %s (%s)
                Created: %s

                Description:
                %s

                Please review and begin working on this ticket.

                Best regards,
                Quick Desk Support System
                """,
                    assignedAgent.getName(),
                    ticket.getTicketId(),
                    ticket.getSubject(),
                    ticket.getCategory().name().replace("_", " "),
                    ticket.getCreatedBy().getName(),
                    ticket.getCreatedBy().getEmail(),
                    ticket.getCreateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")),
                    ticket.getDescription()
            );
            sendSimpleEmail(agentEmail, agentSubject, agentBody);

            logger.info("Assignment emails sent for ticket: {}", ticket.getTicketId());

        } catch (Exception e) {
            logger.error("Failed to send assignment email for ticket: {}", ticket.getTicketId(), e);
        }
    }

    public void sendTicketCreatedEmail(Ticket ticket) {
        if (!emailEnabled) return;

        try {
            String to = ticket.getCreatedBy().getEmail();
            String subject = "Ticket Created Successfully - #" + ticket.getTicketId();
            String body = String.format("""
                Dear %s,

                Your support ticket has been created successfully!

                Ticket ID: #%d
                Subject: %s
                Category: %s
                Status: %s
                Created: %s

                Our support team has been notified and will review your ticket shortly.
                You will receive email notifications about any status updates.

                Thank you for contacting our support team!

                Best regards,
                Quick Desk Support Team
                """,
                    ticket.getCreatedBy().getName(),
                    ticket.getTicketId(),
                    ticket.getSubject(),
                    ticket.getCategory().name().replace("_", " "),
                    ticket.getStatus().name(),
                    ticket.getCreateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))
            );

            sendSimpleEmail(to, subject, body);
            logger.info("Ticket creation email sent for ticket: {} to: {}", ticket.getTicketId(), to);

        } catch (Exception e) {
            logger.error("Failed to send ticket creation email for ticket: {}", ticket.getTicketId(), e);
        }
    }


    private void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
