package org.michael.order.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("t_order_item")
public class OrderItem {

    private Long id;

    private String orderNo;

    private Long skuId;

    private String productTitle;

    private String skuSpec;

    private BigDecimal price;

    private Integer count;
}
