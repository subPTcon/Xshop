package org.michael.product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_sku")
public class Sku {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属商品ID
     */
    private Long productId;

    /**
     * SKU编码
     * 全局唯一
     */
    private String skuCode;

    /**
     * 商品规格JSON
     *
     * 例如：
     * {"颜色":"黑色", "内存":"256G"}
     */
    private String specJson;

    /**
     * SKU价格
     */
    private BigDecimal price;

    /**
     * SKU图片
     */
    private String image;

    /**
     * SKU状态
     * 1: 正常
     * 0: 禁用
     */
    private Integer status;

    private LocalDateTime createTime;


}
