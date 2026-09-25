package org.michael.product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_product")
public class Product {

    /**
     * 商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属类目ID
     */
    private Long categoryId;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品副标题
     */
    private String subTitle;

    /**
     * 商品主图
     */
    private String mainImage;

    /**
     * 商品详情HTML
     */
    private String detailHtml;

    /**
     * 商品状态
     * 1: 上架
     * 0: 下架
     */
    private Integer status;

    /**
     * 销量
     *
     * 冗余字段，异步更新
     */
    private Integer saleCount;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
