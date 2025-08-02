package com.odoo.quick_desk_backend.dto.request;


import com.odoo.quick_desk_backend.utility.TicketStatus;

public class UpdateTicketStatusDto {
    private TicketStatus status;

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
}
