package me.tianxing.interceptor;

import me.tianxing.dao.LoginTicketDAO;
import me.tianxing.dao.UserDAO;
import me.tianxing.model.HostHolder;
import me.tianxing.model.LoginTicket;
import me.tianxing.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by TX on 2016/7/22.
 */
@Component
public class PassportInterceptor implements HandlerInterceptor {
    @Autowired
    LoginTicketDAO loginTicketDAO;

    @Autowired
    UserDAO userDAO;

    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String ticket = null;
        // 先去request的cookies里面找，看有没有ticket的cookie
        if (httpServletRequest.getCookies() != null) {
            for (Cookie cookie : httpServletRequest.getCookies()) {
                if (cookie.getName().equals("ticket")) {
                    ticket = cookie.getValue();
                    break;
                }
            }
        }
        // 如果有的话，就把该user的信息保存到上下文中
        if (ticket != null) {
            LoginTicket loginTicket = loginTicketDAO.selectByTicket(ticket);
            // 如果数据库里没有此ticket 或者 状态不为0 或者 已经过期，那么不能写入User信息
            if (loginTicket == null || loginTicket.getStatus() != 0 || loginTicket.getExpired().before(new Date())) {
                return true;
            }
            // 否则，则将User信息保存到hostHolder中
            User user = userDAO.selectById(loginTicket.getUserId());
            hostHolder.setUser(user);
        }
        // 如果没有，该干啥干啥~~
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && hostHolder.getUser() != null) {
            modelAndView.addObject("user", hostHolder.getUser());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.clear();
    }
}
