package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.List;

/**
 * 菜品数据传输对象
 * 扩展自Dish实体类，用于在前端和后端之间传输菜品相关的完整信息
 */
@Data
public class DishDto extends Dish { 
    
    /**
     * 菜品对应的口味列表
     */
    private List<DishFlavor> flavors;

    /**
     * 菜品所属分类名称
     */
    private String categoryName;
}