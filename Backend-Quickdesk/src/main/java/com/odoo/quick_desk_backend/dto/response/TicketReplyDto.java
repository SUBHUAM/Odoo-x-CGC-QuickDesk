package com.odoo.quick_desk_backend.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TicketReplyDto {
    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;

    public TicketReplyDto() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
