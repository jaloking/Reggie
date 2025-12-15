package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类管理控制器
 * 处理分类相关的HTTP请求
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 查询分类列表
     * @param category 分类查询条件，可指定type类型
     * @return 分类列表数据
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        // 创建条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 根据分类类型查询
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        // 先按排序字段升序，再按更新时间降序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
    
    /**
     * 根据分类ID删除分类
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping
    public R<String> delete(Long id){
        categoryService.remove(id);
        return R.success("分类信息删除成功");
    }
    
    /**
     * 更新分类信息
     * @param category 分类信息
     * @return 更新结果
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }
    
    /**
     * 分页查询分类
     * @param page 当前页码
     * @param pageSize 每页记录数
     * @return 分页查询结果
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        // 创建分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        // 创建条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 按排序字段升序排列
        queryWrapper.orderByAsc(Category::getSort);
        // 执行分页查询
        categoryService.page(pageInfo, queryWrapper);
        
        return R.success(pageInfo);
    }
    
    /**
     * 新增分类
     * @param request HTTP请求对象
     * @param category 分类信息
     * @return 新增结果
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Category category){
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        Long empId = (Long) request.getSession().getAttribute("employee");
        category.setCreateUser(empId);
        category.setUpdateUser(empId);
        
        categoryService.save(category);
        return R.success("新增分类成功");
    }
}
