package org.michael.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.michael.user.pojo.Address;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}
