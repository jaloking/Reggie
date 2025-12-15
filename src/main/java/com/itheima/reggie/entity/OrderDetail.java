package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细实体类
 * 用于存储订单中包含的菜品或套餐的详细信息
 */
@Data
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 菜品或套餐名称
     */
    private String name;

    /**
     * 订单ID
     */
    private Long orderId;

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
}
