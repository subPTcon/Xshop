package org.michael.payment.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("t_payment")
@Data
public class Payment {

    private Long id;

    private String paymentNo;

    private String orderNo;

    private Long userId;

    private BigDecimal amount;

    private Integer payChannel;

    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime createTime;


}
