package org.michael.xshop.service;

import org.michael.xshop.dto.AddAddressRequest;
import org.michael.xshop.dto.AddressResponse;

import java.util.List;

public interface AddressService {

    /**
     * 获取指定用户的地址列表，按“默认地址置顶，其余按创建时间倒序“排序
     */
    List<AddressResponse> listAddresses(Long userId);

    /**
     * 新增收货地址，返回新地址的id
     * 如果 request 里 isDefault = true，会先把该用户名下其他地址的默认标记清掉，保证同一用户永远只有一个默认地址
     */
    Long addAddress(Long userId, AddAddressRequest request);

    /**
     * 修改收货地址。会校验这条地址是否确实属于userId,不属于则抛出ADDRESS_NOT_OWNED
     * 防止用户A传别人的addressId改到别人的地址上（越权修改）
     */
    void updateAddress(Long userId, Long addressId, AddAddressRequest request);

    /**
     * 删除收货地址。同样会校验归属关系，防止越权删除他人地址
     */
    void deleteAddress(Long userId, Long addressId);
}
