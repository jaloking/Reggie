package com.itheima.reggie.common;

/**
 * 基于ThreadLocal封装的工具类，用于在多线程环境下存储和获取当前登录用户的ID
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程的用户ID
     * @param id 用户ID
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前线程的用户ID
     * @return 用户ID
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 移除当前线程的用户ID
     * 用于登出时清理资源，防止内存泄漏
     */
    public static void removeCurrentId() {
        threadLocal.remove();
    }
}