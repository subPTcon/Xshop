package org.michael.order.client.dto;

import lombok.Data;

@Data
public class AddressDTO {

    private Long id;

    private Long userId;

    private String receiverName;

    private String receiverPhone;

    private String province;

    private String city;

    private String district;

    private String detailAddress;


}
