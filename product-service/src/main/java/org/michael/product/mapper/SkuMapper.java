package org.michael.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.michael.product.pojo.ProductMinPrice;
import org.michael.product.pojo.Sku;

import java.util.List;

@Mapper
public interface SkuMapper extends BaseMapper<Sku> {

    List<ProductMinPrice> selectMinPriceByProductIds(
            @Param("productIds") List<Long> productIds
    );
}
