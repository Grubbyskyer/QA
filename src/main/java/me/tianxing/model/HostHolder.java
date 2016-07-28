package me.tianxing.model;

import org.springframework.stereotype.Component;

/**
 * Created by TX on 2016/7/22.
 */
@Component
public class HostHolder {
    // 线程私有的，为每一个登录线程都开辟一个私有的副本
    private static ThreadLocal<User> users = new ThreadLocal<User>();

    public User getUser() {
        return users.get();
    }

    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();
    }

}
