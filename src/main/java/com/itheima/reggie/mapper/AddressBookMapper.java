package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

/**
 * 地址簿Mapper（继承BaseMapper，无需自定义方法）
 */
@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}