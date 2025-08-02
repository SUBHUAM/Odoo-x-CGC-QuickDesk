package com.odoo.quick_desk_backend.dto.response;

import com.odoo.quick_desk_backend.utility.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String name;
    private String email;
    private String username;
    private Instant createTime;
    private Instant updateTime;
    private Role role;
}
