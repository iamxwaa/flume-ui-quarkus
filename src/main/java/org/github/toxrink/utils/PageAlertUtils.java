package org.github.toxrink.utils;

import java.util.Stack;

/**
 * 页面消息提示队列
 * 
 * @author xw
 *
 *         2018年8月24日
 */
public final class PageAlertUtils {

    private static final Stack<String> MSG = new Stack<>();

    /**
     * 写入提示信息,只保留最新的一条
     * 
     * @param msg 提示信息
     */
    public static void writeInfo(String msg) {
        while (!MSG.isEmpty()) {
            MSG.pop();
        }
        MSG.push(msg);
    }

    /**
     * 获取最后一条信息
     * 
     * @return 提示信息
     */
    public static String getMgs() {
        if (MSG.isEmpty()) {
            return null;
        }
        return MSG.pop();
    }
}
