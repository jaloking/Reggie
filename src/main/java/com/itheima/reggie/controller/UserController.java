package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 用户管理控制器
 * 处理用户注册、登录、验证码发送等相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     * @param user 用户对象，包含手机号信息
     * @param session HTTP会话，用于存储验证码
     * @return 发送结果
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        String phone = user.getPhone();
        if (phone == null || phone.trim().isEmpty()) {
            return R.error("手机号不能为空");
        }
        Integer validateCode = ValidateCodeUtils.generateValidateCode(4);
        String code = validateCode.toString();
        log.info("手机号{}的验证码：{}", phone, code);
        session.setAttribute(phone, code);
        return R.success("验证码已生成（控制台查看）");
    }

    /**
     * 用户登录
     * @param map 包含手机号和验证码的Map对象
     * @param session HTTP会话，用于获取验证码
     * @return 登录结果
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session) {
        String phone = map.get("phone");
        String inputCode = map.get("code");
        String sessionCode = (String) session.getAttribute(phone);

        if (sessionCode == null || !sessionCode.equals(inputCode)) {
            return R.error("验证码错误");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User user = userService.getOne(queryWrapper);

        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setStatus(1);
            userService.save(user);
        } else if (user.getStatus() == 0) {
            return R.error("该账号已被禁用，请联系管理员");
        }

        // 核心：使用BaseContext存入用户ID
        BaseContext.setCurrentId(user.getId());
        session.setAttribute("user", user.getId());

        return R.success(user);
    }

    /**
     * 用户登出
     * @param request HTTP请求，用于获取Session
     * @return 登出结果
     */
    @PostMapping("/loginout") // 匹配 POST /user/loginout 请求
    public R<String> loginout(HttpServletRequest request) {
        // 1. 获取Session并清理用户ID
        HttpSession session = request.getSession();
        session.removeAttribute("user"); // 移除Session中的用户ID
        session.invalidate(); // 可选：销毁整个Session

        // 2. 清理ThreadLocal中的用户ID（避免内存泄漏）
        BaseContext.removeCurrentId(); // 需确保BaseContext有removeCurrentId方法

        // 3. 返回登出成功结果
        return R.success("登出成功");
    }
}