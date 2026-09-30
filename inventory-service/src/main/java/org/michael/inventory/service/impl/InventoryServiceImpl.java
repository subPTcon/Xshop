package org.michael.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michael.common.exception.BusinessException;
import org.michael.common.exception.ErrorCode;
import org.michael.common.result.Result;
import org.michael.inventory.client.ProductClient;
import org.michael.inventory.dto.*;
import org.michael.inventory.enums.InventoryChangeType;
import org.michael.inventory.enums.ReservationStatus;
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
    private final InventoryLogMapper inventoryLogMapper;
    private final DefaultRedisScript<Long> inventoryReserveScript;
    private final DefaultRedisScript<Long> inventoryReserveRollbackScript;
    private final DefaultRedisScript<Long> inventoryReleaseScript;

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
            reservation.setStatus(ReservationStatus.RESERVED.getCode());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean release(InventoryReleaseDTO dto) {
        Long skuId = dto.getSkuId();
        String orderNo = dto.getOrderNo();

        // 1.查询预占记录
        InventoryReservation reservation =
                reservationMapper.selectOne(
                        new LambdaQueryWrapper<InventoryReservation>()
                                .eq(
                                        InventoryReservation::getSkuId,
                                        skuId
                                )
                                .eq(
                                        InventoryReservation::getOrderNo,
                                        orderNo
                                )
                );
        if (reservation == null) {
            throw new BusinessException(ErrorCode.INVENTORY_RESERVATION_NOT_FOUND);
        }

        // 2.已经释放，幂等返回成功
        if (reservation.getStatus().equals(ReservationStatus.RELEASED.getCode())) {
            return Boolean.TRUE;
        }

        // 3.已经确认扣减,支付成功后的库存不能再释放
        if (reservation.getStatus().equals(ReservationStatus.CONFIRMED.getCode())) {
            throw new BusinessException(ErrorCode.INVENTORY_RESERVATION_STATUS_INVALID);
        }

        Integer count = reservation.getCount();

        // 4.原子修改 RESERVED -> RELEASED
        int reservationRows =
                reservationMapper.releaseReservation(
                        skuId,
                        orderNo,
                        ReservationStatus.RESERVED.getCode(),
                        ReservationStatus.RELEASED.getCode()
                );
        // 可能被另一个release或confirm抢先修改了，再查一次最终状态
        if (reservationRows == 0) {
            InventoryReservation latest =
                    reservationMapper.selectOne(
                            new LambdaQueryWrapper<InventoryReservation>()
                                    .eq(
                                            InventoryReservation::getSkuId,
                                            skuId
                                    )
                                    .eq(
                                            InventoryReservation::getOrderNo,
                                            orderNo
                                    )
                    );

            if (latest != null && latest.getStatus().equals(ReservationStatus.RELEASED.getCode())) {
                return Boolean.TRUE;
            }

            throw new BusinessException(ErrorCode.INVENTORY_RESERVATION_STATUS_INVALID);
        }

        // 5.MySQL释放锁定库存
        int inventoryRows =
                inventoryMapper.decreaseLockedStock(
                        skuId,
                        count
                );
        if (inventoryRows == 0) {
            /**
             * 抛异常后整个MySQL事务回滚
             *
             * 上面的 RESERVED -> RELEASED 也会回滚成 RESERVED
             */
            throw new BusinessException(
                    ErrorCode.INVENTORY_CONFLICT
            );
        }

        // 6.写流水
        InventoryLog inventoryLog = new InventoryLog();
        inventoryLog.setSkuId(skuId);
        inventoryLog.setOrderNo(orderNo);
        inventoryLog.setChangeType(InventoryChangeType.RELEASE.getCode());
        inventoryLog.setChangeCount(count);
        inventoryLogMapper.insert(inventoryLog);

        // 7.MySQL事务真正提交以后，再恢复Redis可售库存
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        releaseRedisStock(
                                skuId,
                                orderNo,
                                count
                        );
                    }
                }
        );

        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirm(InventoryConfirmDTO dto) {
        Long skuId = dto.getSkuId();
        String orderNo = dto.getOrderNo();

        // 1.查询预占记录
        InventoryReservation reservation =
                reservationMapper.selectOne(
                        new LambdaQueryWrapper<InventoryReservation>()
                                .eq(
                                        InventoryReservation::getSkuId,
                                        skuId
                                )
                                .eq(
                                        InventoryReservation::getOrderNo,
                                        orderNo
                                )
                );
        if (reservation == null) {
            throw new BusinessException(
                    ErrorCode.INVENTORY_RESERVATION_NOT_FOUND
            );
        }

        // 2.已经CONFIRMED
        if (reservation.getStatus().equals(
                ReservationStatus.CONFIRMED.getCode()
        )) {
            return Boolean.TRUE;
        }

        // 3.已经RELEASED
        if (reservation.getStatus().equals(
                ReservationStatus.RELEASED.getCode()
        )) {
            throw new BusinessException(
                    ErrorCode.INVENTORY_RESERVATION_STATUS_INVALID
            );
        }

        Integer count = reservation.getCount();

        // 4.原子状态迁移 RESERVED -> CONFIRMED
        int reservationRows =
                reservationMapper.confirmReservation(
                        skuId,
                        orderNo,
                        ReservationStatus.RESERVED.getCode(),
                        ReservationStatus.CONFIRMED.getCode()
                );
        // 可能同时有 confirm 和 release 操作在竞争这条预占记录
        if (reservationRows == 0) {
            InventoryReservation latest =
                    reservationMapper.selectOne(
                            new LambdaQueryWrapper<InventoryReservation>()
                                    .eq(
                                            InventoryReservation::getSkuId,
                                            skuId
                                    )
                                    .eq(
                                            InventoryReservation::getOrderNo,
                                            orderNo
                                    )
                    );

            // 别的线程已经confirm成功，当前请求按照幂等处理
            if (latest != null && latest.getStatus().equals(ReservationStatus.CONFIRMED.getCode())) {
                return Boolean.TRUE;
            }

            // 如果已经RELEASED，或者出现其他状态，不允许继续confirm
            throw new BusinessException(
                    ErrorCode.INVENTORY_RESERVATION_STATUS_INVALID
            );
        }

        // 5.正式扣减库存
        int inventoryRows =
                inventoryMapper.confirmStock(
                        skuId,
                        count
                );

        if (inventoryRows == 0) {
            /**
             * 抛异常后会回滚
             *
             * 上面的 RESERVED -> CONFIRMED 也会回滚
             */
            throw new BusinessException(
                    ErrorCode.INVENTORY_CONFLICT
            );
        }

        // 6.写库存流水
        InventoryLog inventoryLog = new InventoryLog();
        inventoryLog.setSkuId(skuId);
        inventoryLog.setOrderNo(orderNo);
        inventoryLog.setChangeType(
                InventoryChangeType.CONFIRME.getCode()
        );
        inventoryLog.setChangeCount(count);
        inventoryLogMapper.insert(inventoryLog);

        // 7.MySQL事务成功提交后，清理Redis预占标记，不修改Redis可售库存
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        cleanupConfirmedReservation(skuId, orderNo);
                    }
                }
        );

        return Boolean.TRUE;
    }

    private void cleanupConfirmedReservation(Long skuId, String orderNo) {
        String reserveKey = "inventory:reserved:" + orderNo + ":" + skuId;
        try {
            redisTemplate.delete(reserveKey);
        } catch (Exception e) {
            log.error(
                    "清理Redis库存预占标记失败, skuId={}, orderNo={}",
                    skuId,
                    orderNo,
                    e
            );
        }
    }

    private void releaseRedisStock(Long skuId, String orderNo, Integer count) {
        String stockKey = "inventory:" + skuId;
        String reserveKey = "inventory:reserved:" + orderNo + ":" + skuId;

        try {
            Long result =
                    redisTemplate.execute(
                            inventoryReleaseScript,
                            List.of(
                                    stockKey,
                                    reserveKey
                            ),
                            String.valueOf(count)
                    );
            if (result != null && result == -1L) {
                rebuildInventoryCache(skuId);
            }
        } catch (Exception e) {
            log.error(
                    "释放Redis预占库存失败，skuId={}, orderNo={}, count={}",
                    skuId,
                    orderNo,
                    count,
                    e
            );
        }
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
