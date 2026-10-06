package org.michael.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.michael.payment.pojo.Payment;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
