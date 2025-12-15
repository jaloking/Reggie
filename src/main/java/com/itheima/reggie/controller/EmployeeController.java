package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/**
 * 员工管理控制器
 * 处理员工登录、退出等相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    /**
     * 员工登录
     * @param request HTTP请求对象，用于保存登录状态
     * @param employee 员工登录信息，包含用户名和密码
     * @return 登录结果，成功则返回员工信息，失败则返回错误信息
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        // 1. 将页面提交的密码进行MD5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        
        // 2. 根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        
        // 3. 如果没有查询到员工信息或者密码不正确，则返回登录失败结果
        if (emp == null || !emp.getPassword().equals(password)){
            return R.error("用户名或密码错误！");
        }
        
        // 4. 查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0){
            return R.error("账号已禁用");
        }
        
        // 5. 登录成功，将员工ID存入Session并返回员工信息
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request HTTP请求对象，用于清除登录状态
     * @return 退出结果
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        // 清除Session中保存的当前登录员工的ID
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
}
