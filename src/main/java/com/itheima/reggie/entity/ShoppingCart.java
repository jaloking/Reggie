package com.itheima.reggie.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 * 用于存储用户购物车的商品信息
 */
@Data
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 菜品ID
     */
    private Long dishId;

    /**
     * 套餐ID
     */
    private Long setmealId;

    /**
     * 口味信息
     */
    private String dishFlavor;

    /**
     * 数量
     */
    private Integer number;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 图片路径
     */
    private String image;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
