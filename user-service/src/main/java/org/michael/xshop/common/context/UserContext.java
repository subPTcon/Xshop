package org.michael.xshop.common.context;

/**
 * 当前登录用户上下文。LoginInterceptor在请求进入Controller之前
 * 把从token解析出来的userId存进来；Controller/Service里随时可以取，不用每个方法
 * 都加userId参数
 *
 * 用 ThreadLocal 是因为 Tomcat 对每个请求用独立线程处理，天然隔离，不会串数据
 * 但必须在请求结束时remove，否则线程池复用线程时会导致上一个请求的用户信息“串”到下一个请求
 */
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
