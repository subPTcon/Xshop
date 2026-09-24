package org.michael.xshop.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.michael.xshop.dto.AddressResponse;
import org.michael.xshop.mapper.AddressMapper;
import org.michael.xshop.pojo.Address;
import org.michael.xshop.service.AddressService;
import org.springframework.stereotype.Service;

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
