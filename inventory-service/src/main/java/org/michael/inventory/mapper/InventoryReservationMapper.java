package org.michael.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.michael.inventory.pojo.InventoryReservation;

@Mapper
public interface InventoryReservationMapper extends BaseMapper<InventoryReservation> {

    @Update("""
    UPDATE t_inventory_reservation
    SET status = #{releasedStatus},
        update_time = NOW()
    WHERE sku_id = #{skuId}
    AND order_no = #{orderNo}
    AND status = #{reservedStatus}
""")
    int releaseReservation(
            @Param("skuId") Long skuId,
            @Param("orderNo") String orderNo,
            @Param("reservedStatus") Integer reservedStatus,
            @Param("releasedStatus") Integer releasedStatus
    );

    @Update("""
    UPDATE t_inventory_reservation
    SET status = #{confirmedStatus},
        update_time = NOW()
    WHERE sku_id = #{skuId}
    AND order_no = #{orderNo}
    AND status = #{reservedStatus}
""")
    int confirmReservation(
            @Param("skuId") Long skuId,
            @Param("orderNo") String orderNo,
            @Param("reservedStatus") Integer reservedStatus,
            @Param("confirmedStatus") Integer confirmedStatus
    );
}
