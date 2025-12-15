package com.itheima.reggie.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车Mapper（基于你的实体类）
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
    // 继承BaseMapper，无需自定义方法，自动拥有CRUD
}