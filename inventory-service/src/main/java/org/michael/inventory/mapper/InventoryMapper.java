package org.michael.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.michael.inventory.pojo.Inventory;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Update("""
        UPDATE t_inventory
        SET total_stock = total_stock + #{count},
            version = version + 1
        WHERE sku_id = #{skuId}
""")
    int addStock(
            @Param("skuId") Long skuId,
            @Param("count") Integer count
    );

    @Update("""
    UPDATE t_inventory
    SET locked_stock = locked_stock + #{count},
        version = version + 1
    WHERE sku_id = #{skuId}
""")
    int increaseLockedStock(
            @Param("skuId") Long skuId,
            @Param("count") Integer count
    );

    @Update("""
    UPDATE t_inventory
    SET locked_stock = locked_stock - #{count},
        version = version + 1
    WHERE sku_id = #{skuId}
    AND locked_stock >= #{count}
""")
    int decreaseLockedStock(
            @Param("skuId") Long skuId,
            @Param("count") Integer count
    );

    @Update("""
    UPDATE t_inventory
    SET total_stock = total_stock - #{count},
        locked_stock = locked_stock - #{count},
        version = version + 1
    WHERE sku_id = #{skuId}
    AND total_stock >= #{count}
    AND locked_stock >= #{count}
""")
    int confirmStock(
            @Param("skuId") Long skuId,
            @Param("count") Integer count
    );
}
