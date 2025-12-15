package com.itheima.reggie.mapper;

import  com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.DishFlavor;
import  org.apache.ibatis.annotations.Mapper;

/**
 * 菜品口味数据访问接口
 * 提供菜品口味相关的数据库操作
 */
@Mapper
public  interface  DishFlavorMapper extends  BaseMapper<DishFlavor> {
}
