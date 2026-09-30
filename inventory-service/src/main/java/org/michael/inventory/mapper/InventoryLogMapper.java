package org.michael.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.michael.inventory.pojo.InventoryLog;

@Mapper
public interface InventoryLogMapper extends BaseMapper<InventoryLog> {
}
