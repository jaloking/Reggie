package com.itheima.reggie.service.impl;

import  com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import  com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import  org.springframework.beans.BeanUtils;
import  org.springframework.beans.factory.annotation.Autowired;
import  org.springframework.stereotype.Service;
import  org.springframework.transaction.annotation.Transactional;
import  java.util.List;
import  java.util.stream.Collectors;
@Service
public  class  DishServiceImpl extends  ServiceImpl<DishMapper, Dish>
        implements  DishService {
    @Autowired
    private  DishFlavorService dishFlavorService;
    /**
     * 批量删除菜品，同时删除关联的口味信息
     * @param ids 菜品ID列表
     */
    @Transactional
    public  void  delByIdWithFlavor(List<Long> ids) {
        //检查菜品是否处于在售状态
        LambdaQueryWrapper<Dish> queryWrapper = new  LambdaQueryWrapper();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);
        int  count = this .count(queryWrapper);
        
        //如果有在售菜品，抛出业务异常
        if (count > 0){
            throw  new  CustomException("菜品正在售卖中，不能删除");
        }
        
        //删除dish表基本信息
        this .removeByIds(ids);
        
        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> qw = new  LambdaQueryWrapper();
        qw.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(qw);
    }
    /**
     * 修改菜品信息，同时更新关联的口味信息
     * @param dishDto 菜品数据传输对象，包含菜品基本信息和口味信息
     */
    @Override
    @Transactional
    public  void  updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this .updateById(dishDto);
        
        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new  LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        
        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return  item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
    /**
     * 根据ID查询菜品详情，包括关联的口味信息
     * @param id 菜品ID
     * @return 菜品详情数据传输对象
     */
    public  DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this .getById(id);
        DishDto dishDto = new  DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        
        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new  LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        
        return  dishDto;
    }
    /**
     * 新增菜品，同时保存关联的口味信息
     * @param dishDto 菜品数据传输对象，包含菜品基本信息和口味信息
     */
    @Transactional
    public  void  saveWithFlavor(DishDto dishDto) {
        //新增菜品的基本信息到菜品表dish
        this .save(dishDto);
        
        //获取菜品id
        Long dishId = dishDto.getId();
        
        //处理口味数据，设置菜品id
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return  item;
        }).collect(Collectors.toList());
        
        //新增菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }
}
