package org.michael.xshop.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.michael.xshop.dto.AddAddressRequest;
import org.michael.xshop.dto.AddressResponse;
import org.michael.xshop.mapper.AddressMapper;
import org.michael.xshop.pojo.Address;
import org.michael.xshop.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    @Override
    public List<AddressResponse> listAddresses(Long userId) {
        List<Address> addresses = addressMapper.selectList(
                Wrappers.<Address>lambdaQuery()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getCreateTime)
        );

        if (addresses.isEmpty()) {
            return Collections.emptyList();
        }

        return addresses.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAddress(Long userId, AddAddressRequest request) {
        boolean setAsDefault = Boolean.TRUE.equals(request.getIsDefault());

        // 先清掉该用户名下其他地址的默认标记，保证“最多只有一个默认地址”这个业务约束
        // 放在插入新地址之前做，避免出现短暂的“两个默认地址同时存在”的中间状态
        if (setAsDefault) {
            addressMapper.update(null,
                    Wrappers.<Address>lambdaUpdate()
                            .eq(Address::getUserId, userId)
                            .eq(Address::getIsDefault, 1)
                            .set(Address::getIsDefault, 0)
            );
        }

        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setIsDefault(setAsDefault ? 1 : 0);

        addressMapper.insert(address);
        return address.getId();
    }
    

    private AddressResponse toResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setReceiverName(address.getReceiverName());
        response.setReceiverPhone(address.getReceiverPhone());
        response.setProvince(address.getProvince());
        response.setCity(address.getCity());
        response.setDistrict(address.getDistrict());
        response.setDetailAddress(address.getDetailAddress());
        response.setIsDefault(address.getIsDefault() != null && address.getIsDefault() == 1);
        return response;
    }
}
