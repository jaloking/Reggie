package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿控制器
 * 处理用户地址相关的HTTP请求
 */
@Slf4j // 新增日志注解，便于排查
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 逻辑删除地址
     * 适配前端传参名：ids，匹配前端传递的{ids:this.id}
     * @param id 地址ID
     * @return 删除结果
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") Long id) {
        // 1. 日志打印参数，便于排查
        log.info("接收删除地址请求，地址ID：{}", id);

        // 2. 校验地址ID
        if (id == null) {
            log.error("删除地址失败：地址ID为空");
            return R.error("地址ID不能为空");
        }

        // 3. 获取当前用户ID，增加空值处理（避免400）
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            log.error("删除地址失败：用户未登录，无法获取用户ID");
            return R.error("请先登录后再操作");
        }

        // 4. 逻辑删除：更新is_deleted字段为1
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getId, id);
        updateWrapper.eq(AddressBook::getUserId, userId); // 仅删除当前用户的地址
        updateWrapper.set(AddressBook::getIsDeleted, 1); // 逻辑删除标记

        boolean success = addressBookService.update(updateWrapper);
        if (success) {
            log.info("地址删除成功，地址ID：{}，用户ID：{}", id, userId);
            return R.success("地址删除成功"); // 前端根据该响应跳转
        } else {
            log.error("地址删除失败：地址ID={}，用户ID={}（地址不存在或无权限）", id, userId);
            return R.error("地址删除失败（地址不存在或无权限）");
        }
    }

    /**
     * 查询当前用户的地址列表
     * 过滤逻辑删除的地址
     * @return 地址列表
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return R.error("请先登录后再操作");
        }

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        queryWrapper.eq(AddressBook::getIsDeleted, 0); // 仅查未删除的地址
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        List<AddressBook> list = addressBookService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 新增地址
     * @param addressBook 地址信息
     * @return 新增的地址信息
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return R.error("请先登录后再操作");
        }

        addressBook.setUserId(userId);
        addressBook.setIsDeleted(0); // 新增地址默认未删除
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 更新地址信息
     * @param addressBook 更新后的地址信息
     * @return 更新结果
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return R.error("请先登录后再操作");
        }

        addressBook.setUserId(userId);
        addressBookService.updateById(addressBook);
        return R.success("地址修改成功");
    }

    /**
     * 获取当前用户的默认地址
     * @return 默认地址信息
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return R.error("请先登录后再操作");
        }

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        queryWrapper.eq(AddressBook::getIsDeleted, 0);

        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        if (addressBook == null) {
            return R.error("暂无默认地址");
        }
        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     * @param addressBook 要设置为默认的地址信息
     * @return 设置结果
     */
    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return R.error("请先登录后再操作");
        }

        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getUserId, userId);
        updateWrapper.set(AddressBook::getIsDefault, 0);
        addressBookService.update(updateWrapper);

        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success("设置默认地址成功");
    }

    /**
     * 根据ID获取地址信息
     * @param id 地址ID
     * @return 地址信息
     */
    @GetMapping("/{id}")
    public R<AddressBook> get(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return R.error("请先登录后再操作");
        }

        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook == null || addressBook.getIsDeleted() == 1 || !userId.equals(addressBook.getUserId())) {
            return R.error("地址不存在");
        }
        return R.success(addressBook);
    }
}