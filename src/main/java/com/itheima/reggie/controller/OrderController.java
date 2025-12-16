package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.AddressBookService;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单控制器，处理订单相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    // 新增：注入购物车Service和地址簿Service
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 修改订单状态
     * @param orders 订单信息，包含要修改的状态
     * @return 修改结果
     */
    @PutMapping
    public R<String> update(@RequestBody Orders orders) {
        orderService.updateById(orders);
        return R.success("修改成功");
    }

    /**
     * 管理员端订单分页查询
     * @param page 当前页码
     * @param pageSize 每页记录数
     * @param number 订单号（可选，用于模糊查询）
     * @param beginTime 开始时间（可选，用于时间范围查询）
     * @param endTime 结束时间（可选，用于时间范围查询）
     * @return 订单分页查询结果
     */
    @GetMapping("/page")
    public R<Page<OrdersDto>> page(int page, int pageSize, String number,
                                   String beginTime, String endTime) {
        // 构造分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        // 条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件
        queryWrapper.like(number != null, Orders::getNumber, number);
        queryWrapper.between(beginTime != null && endTime != null,
                Orders::getOrderTime, beginTime, endTime);
        // 添加排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);
        // 执行分页查询
        orderService.page(pageInfo, queryWrapper);

        Page<OrdersDto> ordersDtoPage = new Page<OrdersDto>();
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");

        // 完成订单详情的集合封装
        List<OrdersDto> ordersDtos = pageInfo.getRecords().stream().map(orders -> {
            OrdersDto ordersDto = new OrdersDto();
            // 订单基本信息封装到ordersDto
            BeanUtils.copyProperties(orders, ordersDto);
            // 订单详情集合
            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper =
                    new LambdaQueryWrapper<>();
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, orders.getId());
            List<OrderDetail> ordersDetailList =
                    orderDetailService.list(orderDetailLambdaQueryWrapper);
            ordersDto.setOrderDetails(ordersDetailList);
            return ordersDto;
        }).collect(Collectors.toList());

        // 手动封装ordersDtos分页中的records属性
        ordersDtoPage.setRecords(ordersDtos);
        return R.success(ordersDtoPage);
    }

    /**
     * 用户端订单分页查询
     * @param page 当前页码
     * @param pageSize 每页记录数
     * @param number 订单号（可选，用于模糊查询）
     * @param beginTime 开始时间（可选，用于时间范围查询）
     * @param endTime 结束时间（可选，用于时间范围查询）
     * @return 订单分页查询结果
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(int page, int pageSize, String number,
                                       String beginTime, String endTime) {
        return this.page(page, pageSize, number, beginTime, endTime);
    }

    /**
     * 再次下单
     * @param orders 包含旧订单ID的订单信息
     * @return 再次下单结果
     */
    @PostMapping("/again")
    public R<String> orderAgain(@RequestBody Orders orders) {
        // 1. 校验参数：旧订单ID不能为空
        if (orders.getId() == null) {
            return R.error("旧订单ID不能为空");
        }

        // 2. 查询旧订单信息
        Orders oldOrder = orderService.getById(orders.getId());
        if (oldOrder == null) {
            return R.error("旧订单不存在");
        }

        // 3. 查询旧订单的明细
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OrderDetail::getOrderId, oldOrder.getId());
        List<OrderDetail> oldOrderDetails = orderDetailService.list(detailWrapper);
        if (oldOrderDetails == null || oldOrderDetails.isEmpty()) {
            return R.error("旧订单无商品明细，无法再次下单");
        }

        // 4. 生成新订单
        Orders newOrder = new Orders();
        BeanUtils.copyProperties(oldOrder, newOrder, "id", "orderTime", "status");
        newOrder.setOrderTime(LocalDateTime.now());
        newOrder.setStatus(1); // 待付款
        newOrder.setNumber("NEW_" + System.currentTimeMillis());

        // 5. 保存新订单 + 新订单明细
        orderService.save(newOrder);
        List<OrderDetail> newOrderDetails = oldOrderDetails.stream().map(detail -> {
            OrderDetail newDetail = new OrderDetail();
            BeanUtils.copyProperties(detail, newDetail, "id");
            newDetail.setOrderId(newOrder.getId());
            return newDetail;
        }).collect(Collectors.toList());
        orderDetailService.saveBatch(newOrderDetails);

        return R.success("再次下单成功");
    }

    /**
     * 提交订单
     * @param orders 订单信息，包含地址簿ID等
     * @param session HTTP会话，用于获取当前登录用户信息
     * @return 订单提交结果
     */
    // 记得注入RedisTemplate
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders, HttpSession session) {

        // 如果状态存在且为0（打烊），则禁止下单
        if(ShopController.SHOP_STATUS == 0){
            return R.error("本店已打烊，无法下单，请明天再来！");
        }
        try {
            // 1. 从Session获取登录用户ID
            Long userId = (Long) session.getAttribute("user");
            if (userId == null) {
                return R.error("请先登录");
            }
            log.info("用户{}提交订单，地址ID：{}", userId, orders.getAddressBookId());

            // 2. 查询当前用户的购物车数据
            LambdaQueryWrapper<ShoppingCart> cartWrapper = new LambdaQueryWrapper<>();
            cartWrapper.eq(ShoppingCart::getUserId, userId);
            cartWrapper.orderByAsc(ShoppingCart::getCreateTime);
            List<ShoppingCart> cartList = shoppingCartService.list(cartWrapper);

            // 3. 校验购物车是否为空
            if (cartList == null || cartList.isEmpty()) {
                return R.error("购物车为空，无法下单");
            }

            // 4. 查询收货地址信息
            AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
            if (addressBook == null) {
                return R.error("收货地址不存在，请重新选择");
            }
            // 校验地址归属当前用户
            if (!userId.equals(addressBook.getUserId())) {
                return R.error("收货地址不属于当前用户，请重新选择");
            }

            // 5. 组装订单核心数据
            Orders newOrder = new Orders();
            BeanUtils.copyProperties(orders, newOrder);
            newOrder.setId(null); // 主键自增
            newOrder.setUserId(userId); // 关联当前用户
            newOrder.setNumber("ORDER_" + System.currentTimeMillis() + "_" + userId); // 唯一订单号
            newOrder.setOrderTime(LocalDateTime.now()); // 下单时间
            newOrder.setCheckoutTime(LocalDateTime.now()); // 修复：新增结算时间赋值
            newOrder.setStatus(4); // 待付款
            // 地址信息赋值
            newOrder.setConsignee(addressBook.getConsignee());
            newOrder.setPhone(addressBook.getPhone());
            newOrder.setAddress(
                    addressBook.getProvinceName() + addressBook.getCityName() +
                            addressBook.getDistrictName() + addressBook.getDetail()
            );

            // 6. 计算订单总金额
            BigDecimal totalAmount = cartList.stream()
                    .map(cart -> cart.getAmount().multiply(new BigDecimal(cart.getNumber())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            newOrder.setAmount(totalAmount);
            log.info("订单{}总金额：{}", newOrder.getNumber(), totalAmount);

            // 7. 保存订单
            orderService.save(newOrder);

            // 8. 组装并保存订单明细
            List<OrderDetail> detailList = cartList.stream().map(cart -> {
                OrderDetail detail = new OrderDetail();
                detail.setOrderId(newOrder.getId());
                detail.setName(cart.getName());
                detail.setImage(cart.getImage());
                detail.setDishId(cart.getDishId());
                detail.setSetmealId(cart.getSetmealId());
                detail.setDishFlavor(cart.getDishFlavor());
                detail.setNumber(cart.getNumber());
                detail.setAmount(cart.getAmount());
                return detail;
            }).collect(Collectors.toList());
            orderDetailService.saveBatch(detailList);
            log.info("订单{}保存明细{}条", newOrder.getNumber(), detailList.size());

            // 9. 清空购物车
            shoppingCartService.remove(cartWrapper);
            log.info("用户{}购物车已清空", userId);

            return R.success("下单成功！订单号：" + newOrder.getNumber());
        } catch (Exception e) {
            log.error("提交订单失败", e);
            return R.error("提交订单失败：" + e.getMessage());
        }
    }
}