package org.michael.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.common.result.Result;
import org.michael.inventory.client.ProductClient;
import org.michael.inventory.dto.InventoryInitDTO;
import org.michael.inventory.dto.SkuDTO;
import org.michael.inventory.mapper.InventoryMapper;
import org.michael.inventory.pojo.Inventory;
import org.michael.inventory.service.InventoryService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final StringRedisTemplate redisTemplate;
    private final ProductClient productClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initInventory(InventoryInitDTO dto) {
        Long skuId = dto.getSkuId();

        // 1.确认 SKU 存在
        validateSku(skuId);

        // 2.检查是否已经初始化
        Long count = inventoryMapper.selectCount(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getSkuId, skuId)
        );

        if (count > 0) {
            throw new BusinessException(
                    ErrorCode.INVENTORY_ALREADY_EXISTS
            );
        }

        // 3.初始化MySQL库存
        Inventory inventory = new Inventory();
        inventory.setSkuId(skuId);
        inventory.setTotalStock(dto.getStock());
        inventory.setLockedStock(0);
        inventory.setVersion(0);

        try {
            inventoryMapper.insert(inventory);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(
                    ErrorCode.INVENTORY_ALREADY_EXISTS
            );
        }

        // 4.MySQL事务成功提交后，再初始化Redis库存
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        String key = "inventory:" + skuId;
                        redisTemplate.opsForValue()
                                .set(
                                        key,
                                        String.valueOf(dto.getStock())
                                );
                    }
                }
        );
    }

    private void validateSku(Long skuId) {
        Result<SkuDTO> result;

        try {
            result = productClient.getSkuById(skuId);
        } catch (Exception e) {
            log.error("调用product-service查询SKU失败，skuId={}", skuId, e);
            throw new BusinessException(
                    ErrorCode.REMOTE_SERVICE_ERROR
            );
        }

        if (result == null || result.getData() == null) {
            throw new BusinessException(
                    ErrorCode.SKU_NOT_FOUND
            );
        }
    }
}
