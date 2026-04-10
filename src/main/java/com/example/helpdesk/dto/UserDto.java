package com.example.helpdesk.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Integer id;
    private String username;
    private String fullName;
    private String role;
}
