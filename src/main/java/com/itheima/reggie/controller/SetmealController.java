package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理控制器
 * 处理套餐相关的HTTP请求
 */
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 删除套餐
     * @param ids 套餐ID列表
     * @return 操作结果
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    /**
     * 更新套餐状态
     * @param statusNum 状态值 0:停售 1:起售
     * @param ids 套餐ID列表
     * @return 操作结果
     */
    @PostMapping("/status/{statusNum}")
    public R<String> updateStatus(@PathVariable Integer statusNum,
                                   @RequestParam List<Long> ids){
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Setmeal::getId, ids);
        updateWrapper.set(Setmeal::getStatus, statusNum);
        setmealService.update(updateWrapper);
        return R.success(statusNum == 0 ? "停售成功" : "起售成功");
    }
    
    /**
     * 修改套餐
     * @param setmealDto 套餐数据传输对象
     * @return 操作结果
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("修改套餐成功");
    }
    
    /**
     * 根据套餐id查询套餐信息和对应的菜品信息
     * @param id 套餐ID
     * @return 套餐详细信息
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }
    
    /**
     * 套餐分页查询
     * @param page 页码
     * @param pageSize 每页条数
     * @param name 套餐名称（模糊查询）
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        // 分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();
        
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null, Setmeal::getName, name);
        // 添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        
        setmealService.page(pageInfo, queryWrapper);
        
        // 对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            // 对象拷贝
            BeanUtils.copyProperties(item, setmealDto);
            // 分类id
            Long categoryId = item.getCategoryId();
            // 根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null){
                // 分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());
        
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }
    
    /**
     * 新增套餐
     * @param setmealDto 套餐数据传输对象
     * @return 操作结果
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 查询指定分类下的启售套餐
     * @param categoryId 分类ID
     * @return 套餐列表
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(@RequestParam Long categoryId) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId, categoryId); // 匹配分类ID
        queryWrapper.eq(Setmeal::getStatus, 1); // 只查启售状态
        queryWrapper.orderByDesc(Setmeal::getUpdateTime); // 按更新时间排序

        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        return R.success(setmealList);
    }
}