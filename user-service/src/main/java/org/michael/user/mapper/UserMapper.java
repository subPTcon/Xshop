package org.michael.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.michael.user.pojo.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {


}
