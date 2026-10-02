package org.michael.common.context;

public class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private UserContext() {}

    public static void setUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static long getUserId() {
        return CURRENT_USER_ID.get();
    }

    /**
     * 必须在请求处理完成后调用
     * 防止线程池复用线程时数据串到下一个请求
     */
    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
