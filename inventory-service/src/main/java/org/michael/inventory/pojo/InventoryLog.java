package org.michael.inventory.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_inventory_log")
public class InventoryLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skuId;

    private String orderNo;

    /**
     * 库存变化类型
     *
     * 1: 预扣
     * 2: 释放
     * 3: 真实扣减
     * 4: 回滚
     */
    private Integer changeType;

    /**
     * 变化数量
     */
    private Integer changeCount;

    private LocalDateTime createTime;
}
