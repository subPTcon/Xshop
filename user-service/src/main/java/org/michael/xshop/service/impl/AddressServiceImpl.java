package org.michael.xshop.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.michael.xshop.common.exception.BusinessException;
import org.michael.xshop.common.exception.ErrorCode;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long userId, Long addressId, AddAddressRequest request) {
        Address existing = addressMapper.selectById(addressId);

        // 地址压根不存在(id写错、或者已经被删了），跟“存在但不是你的”区分开报不同的错误码
        if (existing == null) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        // 核心越权校验：这条地址的userId必须等于当前登录用户，否则拒绝修改
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_OWNED);
        }

        boolean setAsDefault = Boolean.TRUE.equals(request.getIsDefault());

        if (setAsDefault) {
            addressMapper.update(null,
                    Wrappers.<Address>lambdaUpdate()
                            .eq(Address::getUserId, userId)
                            .eq(Address::getIsDefault, 1)
                            .ne(Address::getId, addressId)
                            .set(Address::getIsDefault, 0)
            );
        }

        existing.setReceiverName(request.getReceiverName());
        existing.setReceiverPhone(request.getReceiverPhone());
        existing.setProvince(request.getProvince());
        existing.setCity(request.getCity());
        existing.setDistrict(request.getDistrict());
        existing.setDetailAddress(request.getDetailAddress());
        existing.setIsDefault(setAsDefault ? 1 : 0);

        addressMapper.updateById(existing);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        Address existing = addressMapper.selectById(addressId);

        // 删除接口对"不存在"做幂等处理：如果这条地址已经不存在了（比如用户手快连点两次删除），
        // 直接当成功返回，而不是报ADDRESS_NOT_FOUND——最终状态是一致的("这条地址不存在")
        if (existing == null) {
            return;
        }

        // 但如果地址存在、只是不属于当前用户，必须严格拒绝，这是越权删除的红线，不能做成"静默跳过"
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_OWNED);
        }

        addressMapper.deleteById(addressId);
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
