package com.itheima.reggie.common;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用返回结果类
 * 服务端响应的数据最终都会封装成此对象
 * @param <T> 数据类型
 */
@Data
public class R<T> {

    /**
     * 编码：1成功，0和其它数字为失败
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 动态扩展数据
     */
    private Map map = new HashMap();

    /**
     * 成功响应方法
     * @param object 响应数据
     * @param <T> 数据类型
     * @return 封装后的响应对象
     */
    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    /**
     * 错误响应方法
     * @param msg 错误信息
     * @param <T> 数据类型
     * @return 封装后的响应对象
     */
    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    /**
     * 添加动态扩展数据
     * @param key 键
     * @param value 值
     * @return 当前对象，用于链式调用
     */
    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
