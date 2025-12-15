package com.itheima.reggie.service;

import  com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import  java.util.List;

public  interface  DishService extends  IService<Dish> {
    /**
     * 保存菜品信息，同时保存关联的口味信息
     * @param dishDto 菜品数据传输对象，包含菜品基本信息和口味信息
     */
    public  void  saveWithFlavor(DishDto dishDto);
    
    /**
     * 根据菜品ID查询菜品详情，包括关联的口味信息
     * @param id 菜品ID
     * @return 菜品数据传输对象，包含菜品基本信息和口味信息
     */
    public  DishDto getByIdWithFlavor(Long id);
    
    /**
     * 更新菜品信息，同时更新关联的口味信息
     * @param dishDto 菜品数据传输对象，包含菜品基本信息和口味信息
     */
    public  void  updateWithFlavor(DishDto dishDto);
    
    /**
     * 批量删除菜品信息，同时删除关联的口味信息
     * @param ids 菜品ID列表
     */
    public  void  delByIdWithFlavor(List<Long> ids);
}
