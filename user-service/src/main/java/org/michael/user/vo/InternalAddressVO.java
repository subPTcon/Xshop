package org.michael.user.vo;

import lombok.Data;

@Data
public class InternalAddressVO {

    private Long id;

    private Long userId;

    private String receiverName;

    private String receiverPhone;

    private String province;

    private String city;

    private String district;

    private String detailAddress;


}
