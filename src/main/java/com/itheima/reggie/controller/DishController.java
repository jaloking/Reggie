package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理控制器，处理菜品相关的HTTP请求
 */
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;

    // 注入口味服务（关键：补充@Autowired注解）
    @Autowired
    private DishFlavorService dishFlavorService; // 现在能正确识别了


    /**
     * 查询指定分类下的启售菜品，包含完整口味信息（匹配DishFlavor实体）
     * @param categoryId 分类ID
     * @return 包含口味的菜品列表（DishDto）
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(@RequestParam Long categoryId) {
        // 1. 查询分类下所有启售的菜品（status=1）
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getCategoryId, categoryId); // 匹配分类ID
        dishWrapper.eq(Dish::getStatus, 1); // 只查启售状态
        dishWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime); // 排序
        List<Dish> dishList = dishService.list(dishWrapper);

        // 2. 为每个菜品查询对应的口味，并封装到DishDto
        List<DishDto> dishDtoList = dishList.stream().map(dish -> {
            DishDto dishDto = new DishDto();
            // 复制菜品基本信息到DishDto
            BeanUtils.copyProperties(dish, dishDto);

            // 3. 查询当前菜品的所有口味（根据dishId关联）
            LambdaQueryWrapper<DishFlavor> flavorWrapper = new LambdaQueryWrapper<>();
            flavorWrapper.eq(DishFlavor::getDishId, dish.getId()); // 关联菜品ID
            List<DishFlavor> flavorList = dishFlavorService.list(flavorWrapper);

            // 4. 将口味列表设置到DishDto（DishDto需包含List<DishFlavor> flavors字段）
            dishDto.setFlavors(flavorList);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }


    /**
     * 批量删除菜品，同时删除关联的口味信息
     * @param ids 菜品ID列表
     * @return 删除结果
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        dishService.delByIdWithFlavor(ids);
        return R.success("删除成功");
    }

    /**
     * 批量更新菜品状态（起售/停售）
     * @param statusNum 状态值（0：停售，1：起售）
     * @param ids 菜品ID列表
     * @return 更新结果
     */
    @PostMapping("/status/{statusNum}")
    public R<String> updateStatus(@PathVariable Integer statusNum,
                                   @RequestParam List<Long> ids){
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Dish::getId, ids);
        updateWrapper.set(Dish::getStatus, statusNum);
        dishService.update(updateWrapper);
        return R.success(statusNum == 0 ? "停售成功" : "起售成功");
    }
    /**
     * 修改菜品信息，同时更新关联的口味信息
     * @param dishDto 菜品数据传输对象，包含菜品基本信息和口味信息
     * @return 修改结果
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }
    /**
     * 根据ID查询菜品详情，包括关联的口味信息
     * @param id 菜品ID
     * @return 菜品详情数据传输对象
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }
    /**
     * 菜品信息分页查询
     * @param page 当前页码
     * @param pageSize 每页记录数
     * @param name 菜品名称（可选，用于模糊查询）
     * @return 分页查询结果
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        
        //添加过滤条件（名称模糊查询）
        queryWrapper.like(name != null, Dish::getName, name);
        
        //添加排序条件（按更新时间倒序）
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        
        //执行分页查询
        dishService.page(pageInfo, queryWrapper);
        
        //对象拷贝（排除records字段，需要单独处理）
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        
        //处理菜品记录，转换为DishDto并补充分类名称
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            
            //根据分类id查询分类名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            
            return dishDto;
        }).collect(Collectors.toList());
        
        //设置转换后的记录到分页对象
        dishDtoPage.setRecords(list);
        
        return R.success(dishDtoPage);
    }
    /**
     * 新增菜品，同时保存关联的口味信息
     * @param dishDto 菜品数据传输对象，包含菜品基本信息和口味信息
     * @return 新增结果
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }
}
