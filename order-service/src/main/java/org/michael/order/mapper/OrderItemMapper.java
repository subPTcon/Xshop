package org.michael.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.michael.order.pojo.OrderItem;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
