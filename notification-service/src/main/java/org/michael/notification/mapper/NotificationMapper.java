package org.michael.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.michael.notification.pojo.Notification;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
