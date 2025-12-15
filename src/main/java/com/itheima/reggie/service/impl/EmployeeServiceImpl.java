package com.itheima.reggie.service.impl;

import  com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.service.EmployeeService;
import  org.springframework.stereotype.Service;

/**
 * 员工服务实现类
 * 实现员工相关的业务逻辑操作
 */
@Service
public  class  EmployeeServiceImpl extends  ServiceImpl<EmployeeMapper, Employee>
        implements  EmployeeService{
}
