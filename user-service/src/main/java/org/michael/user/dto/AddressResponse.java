package org.michael.user.dto;

import lombok.Data;

@Data
public class AddressResponse {
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Boolean isDefault;
}
