package org.michael.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.michael.inventory.pojo.InventoryReservation;

@Mapper
public interface InventoryReservationMapper extends BaseMapper<InventoryReservation> {
}
