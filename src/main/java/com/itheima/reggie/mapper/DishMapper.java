package com.itheima.reggie.mapper;

import  com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Dish;
import  org.apache.ibatis.annotations.Mapper;

/**
 * 菜品数据访问接口
 * 提供菜品相关的数据库操作
 */
@Mapper
public  interface  DishMapper extends  BaseMapper<Dish> {
}