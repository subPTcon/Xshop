package org.michael.xshop.service;

import org.michael.xshop.dto.AddressResponse;

import java.util.List;

public interface AddressService {

    /**
     * 获取指定用户的地址列表，按“默认地址置顶，其余按创建时间倒序“排序
     */
    List<AddressResponse> listAddresses(Long userId);

}
