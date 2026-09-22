package org.michael.xshop.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("t_user")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String email;

    private String avatar;

    @TableField(
            insertStrategy = FieldStrategy.NOT_NULL,
            updateStrategy = FieldStrategy.NOT_NULL
    )
    private Integer status;

    @TableField(
            value = "create_time",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER
    )
    private LocalDateTime createTime;

    @TableField(
            value = "update_time",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER
    )
    private LocalDateTime updateTime;
}
