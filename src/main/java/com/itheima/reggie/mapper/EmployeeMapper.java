package com.itheima.reggie.mapper;

import  com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Employee;
import  org.apache.ibatis.annotations.Mapper;

/**
 * 员工数据访问接口
 * 提供员工相关的数据库操作
 */
@Mapper
public  interface  EmployeeMapper extends  BaseMapper<Employee>{
}