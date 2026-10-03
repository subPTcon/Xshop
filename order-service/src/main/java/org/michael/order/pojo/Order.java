package org.michael.order.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {

    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    /**
     * 0 待支付
     * 1 已支付
     * 2 待发货
     * 3 已发货
     * 4 已完成
     * 5 已取消
     */
    private Integer status;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private LocalDateTime payTime;

    private String cancelReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
