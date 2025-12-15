package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车控制器
 * 处理用户购物车相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 获取购物车列表
     * 前端请求路径：GET /shoppingCart/list
     * @param session HTTP会话，用于获取登录用户信息
     * @return 购物车列表
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(HttpSession session) {
        // 获取登录用户ID（登录时存入Session的user属性）
        Long userId = (Long) session.getAttribute("user");



        // 查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> cartList = shoppingCartService.list(queryWrapper);
        return R.success(cartList);
    }

    /**
     * 添加商品到购物车
     * 前端请求路径：POST /shoppingCart/add
     * @param shoppingCart 购物车商品信息
     * @param session HTTP会话，用于获取登录用户信息
     * @return 添加后的购物车商品信息
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart, HttpSession session) {
        Long userId = (Long) session.getAttribute("user");
        // 未登录则提示，不返回404
        if (userId == null) {
            return R.error("请先登录");
        }

        // 填充用户ID和创建时间
        shoppingCart.setUserId(userId);
        shoppingCart.setCreateTime(LocalDateTime.now());

        // 判断当前商品是否已在购物车（菜品/套餐+口味唯一）
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        // 菜品ID不为空则匹配菜品
        queryWrapper.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        // 套餐ID不为空则匹配套餐
        queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        // 口味不为空则匹配口味
        queryWrapper.eq(shoppingCart.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());

        ShoppingCart existCart = shoppingCartService.getOne(queryWrapper);
        if (existCart != null) {
            // 已存在则数量+1
            existCart.setNumber(existCart.getNumber() + 1);
            shoppingCartService.updateById(existCart);
            return R.success(existCart);
        }

        // 不存在则新增，数量默认1
        shoppingCart.setNumber(1);
        shoppingCartService.save(shoppingCart);
        return R.success(shoppingCart);
    }

    /**
     * 减少购物车商品数量
     * 前端请求路径：POST /shoppingCart/sub
     * @param shoppingCart 购物车商品信息
     * @param session HTTP会话，用于获取登录用户信息
     * @return 更新后的购物车商品信息
     */
    @PostMapping("/sub")
    public R<ShoppingCart> subtract(@RequestBody ShoppingCart shoppingCart, HttpSession session) {
        Long userId = (Long) session.getAttribute("user");
        if (userId == null) {
            return R.error("请先登录");
        }

        // 查询要减少的商品
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

        ShoppingCart existCart = shoppingCartService.getOne(queryWrapper);
        if (existCart == null) {
            return R.error("购物车中无此商品");
        }

        // 数量-1，小于等于0则删除
        existCart.setNumber(existCart.getNumber() - 1);
        if (existCart.getNumber() <= 0) {
            shoppingCartService.removeById(existCart.getId());
            existCart.setNumber(0); // 前端需要感知数量为0
        } else {
            shoppingCartService.updateById(existCart);
        }
        return R.success(existCart);
    }

    /**
     * 清空购物车
     * 前端请求路径：DELETE /shoppingCart/clean
     * @param session HTTP会话，用于获取登录用户信息
     * @return 清空结果
     */
    @DeleteMapping("/clean")
    public R<String> clean(HttpSession session) {
        Long userId = (Long) session.getAttribute("user");
        if (userId == null) {
            return R.error("请先登录");
        }

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(queryWrapper);

        return R.success("购物车已清空");
    }
}
