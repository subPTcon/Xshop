package org.michael.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.michael.inventory.pojo.Inventory;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
}
