package org.michael.xshop.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_category")
public class Category {

    /**
     * 类目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父类目ID
     * 0表示一级类目
     */
    private Long parentId;

    /**
     * 类目名称
     */
    private String name;

    /**
     * 排序值
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
