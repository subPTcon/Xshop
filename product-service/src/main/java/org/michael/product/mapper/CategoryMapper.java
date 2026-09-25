package org.michael.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.michael.product.pojo.Category;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
