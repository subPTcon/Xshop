package org.michael.inventory.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_inventory")
public class Inventory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SKU ID
     *
     * 一个SKU对应一条库存记录
     */
    private Long skuId;

    /**
     * 实际总库存
     */
    private Integer totalStock;

    /**
     * 已预占但尚未最终确认的库存
     *
     * 例如：
     * 下单成功，但订单尚未支付
     */
    private Integer lockedStock;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
