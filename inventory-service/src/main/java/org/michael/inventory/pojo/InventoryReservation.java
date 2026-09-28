package org.michael.inventory.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 记录“某个订单，对某个SKU，到底预占了多少库存，目前处于什么状态"
 */
@Data
@TableName("t_inventory_reservation")
public class InventoryReservation {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skuId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 该订单预占的库存数量
     */
    private Integer count;

    /**
     * 预占状态
     *
     * 0: 已预占
     * 1: 已确认扣减
     * 2: 已释放
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
