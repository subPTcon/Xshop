package org.michael.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.common.result.Result;
import org.michael.inventory.client.ProductClient;
import org.michael.inventory.dto.InventoryAddDTO;
import org.michael.inventory.dto.InventoryInitDTO;
import org.michael.inventory.dto.ReserveInventoryDTO;
import org.michael.inventory.dto.SkuDTO;
import org.michael.inventory.mapper.InventoryLogMapper;
import org.michael.inventory.mapper.InventoryMapper;
import org.michael.inventory.mapper.InventoryReservationMapper;
import org.michael.inventory.pojo.Inventory;
import org.michael.inventory.pojo.InventoryLog;
import org.michael.inventory.pojo.InventoryReservation;
import org.michael.inventory.service.InventoryService;
import org.michael.inventory.vo.InventoryReserveVO;
import org.michael.inventory.vo.InventoryStockVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final StringRedisTemplate redisTemplate;
    private final ProductClient productClient;
    private final InventoryReservationMapper reservationMapper;
    private final DefaultRedisScript<Long> inventoryReserveScript;
    private final DefaultRedisScript<Long> inventoryReserveRollbackScript;
    private final InventoryLogMapper inventoryLogMapper;

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

    @Override
    public InventoryStockVO getAvailableStock(Long skuId) {
        String key = "inventory:" + skuId;

        // 1.优先查 Redis
        String stockValue = redisTemplate.opsForValue().get(key);
        if (stockValue != null) {
            return new InventoryStockVO(skuId, Integer.valueOf(stockValue));
        }

        // 2.Redis没有，回源 MySQL
        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getSkuId, skuId)
        );
        if (inventory == null) {
            throw new BusinessException(ErrorCode.INVENTORY_NOT_FOUND);
        }

        // 3.计算可售库存
        int availableStock = inventory.getTotalStock() - inventory.getLockedStock();

        // 4.回填 Redis
        redisTemplate.opsForValue().set(
                key, String.valueOf(availableStock)
        );

        return new InventoryStockVO(skuId, availableStock);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStock(InventoryAddDTO dto) {
        Long skuId = dto.getSkuId();
        Integer count = dto.getCount();

        // 1. MySQL原子增加总库存
        int affectedRows = inventoryMapper.addStock(
                skuId,
                count
        );

        // 2.没有更新到任何记录，说明库存未初始化
        if (affectedRows == 0) {
            throw new BusinessException(ErrorCode.INVENTORY_NOT_FOUND);
        }

        // 3.数据库事务提交成功后更新Redis
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        String key = "inventory:" + skuId;

                        Boolean exists = redisTemplate.hasKey(key);

                        if (Boolean.TRUE.equals(exists)) {
                            redisTemplate.opsForValue().increment(key, count);
                        } else {
                            rebuildInventoryCache(skuId);
                        }
                    }
                }
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InventoryReserveVO reserve(
            ReserveInventoryDTO dto
    ) {
        Long skuId = dto.getSkuId();
        String orderNo = dto.getOrderNo();
        Integer count = dto.getCount();

        String stockKey = "inventory:" + skuId;
        String reserveKey = "inventory:reserved:" + orderNo + ":" + skuId;

        // 1.Redis Lua原子预扣
        Long result = redisTemplate.execute(
                inventoryReserveScript,
                List.of(stockKey, reserveKey),
                String.valueOf(count),
                String.valueOf(30 * 60)
        );
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 2.库存不足
        if (result == 0L) {
            return new InventoryReserveVO(false, "库存不足");
        }

        // 3.Redis库存不存在
        if (result == -1L) {
            throw new BusinessException(ErrorCode.INVENTORY_NOT_FOUND);
        }

        // 4.已经预占过
        if (result == 2L) {
            return new InventoryReserveVO(true, "库存已预占，请勿重复提交");
        }

        try {
            // 5.MySQL增加locked_stock
            int affectedRows = inventoryMapper.increaseLockedStock(skuId, count);
            if (affectedRows == 0) {
                throw new BusinessException(ErrorCode.INVENTORY_NOT_FOUND);
            }

            // 6.写预占记录
            InventoryReservation reservation = new InventoryReservation();
            reservation.setSkuId(skuId);
            reservation.setOrderNo(orderNo);
            reservation.setCount(count);
            reservation.setStatus(0);
            reservationMapper.insert(reservation);

            // 7.写库存流水
            InventoryLog inventoryLog = new InventoryLog();
            inventoryLog.setSkuId(skuId);
            inventoryLog.setOrderNo(orderNo);
            inventoryLog.setChangeType(1);
            inventoryLog.setChangeCount(count);
            inventoryLogMapper.insert(inventoryLog);
        } catch (Exception e) {
            /**
             * Redis已经扣了
             * 但MySQL持久化失败
             *
             * 必须把Redis库存补回来
             */
            rollbackRedisReserve(stockKey, reserveKey, count);

            if (e instanceof BusinessException businessException) {
                throw businessException;
            }

            if (e instanceof DuplicateKeyException) {
                return new InventoryReserveVO(
                        true,
                        "库存已预占，请勿重复提交"
                );
            }

            throw e;
        }

        return new InventoryReserveVO(true, null);
    }

    private void rollbackRedisReserve(
            String stockKey,
            String reserveKey,
            Integer count
    ) {
        try {
            redisTemplate.execute(
                    inventoryReserveRollbackScript,
                    List.of(stockKey, reserveKey),
                    String.valueOf(count)
            );
        } catch (Exception e) {
            log.error(
                    "Redis库存补偿失败, stockKey={}, reserveKey={}",
                    stockKey,
                    reserveKey,
                    e
            );
        }
    }

    private void rebuildInventoryCache(Long skuId) {
        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getSkuId, skuId)
        );

        if (inventory == null) {
            return;
        }

        int availableStock = inventory.getTotalStock() - inventory.getLockedStock();

        redisTemplate.opsForValue().set(
                "inventory:" + skuId,
                String.valueOf(availableStock)
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
