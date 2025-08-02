package com.odoo.quick_desk_backend.dto.request;

import com.odoo.quick_desk_backend.utility.VoteType;
import jakarta.validation.constraints.NotNull;

public class VoteDto {

    @NotNull(message = "Vote type is required")
    private VoteType voteType;

    public VoteDto() {}

    public VoteType getVoteType() { return voteType; }
    public void setVoteType(VoteType voteType) { this.voteType = voteType; }
}
