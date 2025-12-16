package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
@Slf4j
public class ShopController {

    // 核心修改：使用静态变量替代 Redis 存储，内存共享
    // 默认 1 为营业中
    public static Integer SHOP_STATUS = 1;

    /**
     * 设置营业状态
     * @param status 1-营业中，0-打烊中
     */
    @PutMapping("/{status}")
    public R<String> setStatus(@PathVariable Integer status){
        log.info("设置店铺营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        SHOP_STATUS = status; // 修改静态变量
        return R.success("店铺营业状态设置成功");
    }

    /**
     * 获取营业状态
     */
    @GetMapping("/status")
    public R<Integer> getStatus(){
        return R.success(SHOP_STATUS); // 返回静态变量
    }
}