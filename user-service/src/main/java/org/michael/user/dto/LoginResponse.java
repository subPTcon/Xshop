package org.michael.user.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    private Long expireIn;

    private Long userId;
}
