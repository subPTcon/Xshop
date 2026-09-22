package org.michael.xshop.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    private Long expireIn;

    private Long userId;
}
