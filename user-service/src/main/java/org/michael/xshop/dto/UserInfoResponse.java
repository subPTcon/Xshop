package org.michael.xshop.dto;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
}
