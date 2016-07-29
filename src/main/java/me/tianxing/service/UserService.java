package me.tianxing.service;

import me.tianxing.constants.StringConstants;
import me.tianxing.dao.LoginTicketDAO;
import me.tianxing.dao.UserDAO;
import me.tianxing.model.LoginTicket;
import me.tianxing.model.User;
import me.tianxing.util.GeetestLib;
import me.tianxing.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;

/**
 * Created by TX on 2016/7/21.
 */
@Service
public class UserService {

    @Autowired
    UserDAO userDAO;

    @Autowired
    LoginTicketDAO loginTicketDAO;

    // geetest二次验证结果
    public int geetest(String challenge, String validate, String seccode,
                       int gt_server_status_code, String userid) {
        int gtResult = 0;
        GeetestLib gtSdk = new GeetestLib(StringConstants.CAPTCHA_ID, StringConstants.PRIVATE_KEY);
        if (gt_server_status_code == 1) {
            //gt-server正常，向gt-server进行二次验证

            gtResult = gtSdk.enhencedValidateRequest(challenge, validate, seccode, userid);
//            System.out.println(gtResult);
        } else {
            // gt-server非正常情况下，进行failback模式验证

//            System.out.println("failback:use your own server captcha validate");
            gtResult = gtSdk.failbackValidateRequest(challenge, validate, seccode);
//            System.out.println(gtResult);
            gtResult = 1; // 这里没有备用机制，先置为成功
        }
        return gtResult;
    }

    // 注册功能
    public Map<String, String> register(String username, String password) {
        Map<String, String> map = new HashMap<String, String>();
        // 用户名为空
        if (StringUtils.isBlank(username)) {
            map.put("msg", StringConstants.EMPTY_USERNAME);
            return map;
        }
        // 密码为空
        if (StringUtils.isBlank(password)) {
            map.put("msg", StringConstants.EMPTY_PASSWORD);
            return map;
        }
        // 用户名已经被注册
        User user = userDAO.selectByName(username);
        if (user != null) {
            map.put("msg", StringConstants.USERNAME_ALREADY_EXISTS);
            return map;
        }
        // 满足注册条件，添加到数据库中
        user = new User();
        user.setName(username);
        // 生成一个随机的盐，提高密码强度
        user.setSalt(UUID.randomUUID().toString().substring(0, 5));
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        // md5 加密 加盐后的密码，数据库里不保存明文密码
        user.setPassword(WendaUtil.MD5(password + user.getSalt()));
        userDAO.addUser(user);

        // 为该user分发一个ticket
        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);

        return map;

    }

    // 登录功能
    public Map<String, String> login(String username, String password) {
        Map<String, String> map = new HashMap<String, String>();
        // 用户名为空
        if (StringUtils.isBlank(username)) {
            map.put("msg", StringConstants.EMPTY_USERNAME);
            return map;
        }
        // 密码为空
        if (StringUtils.isBlank(password)) {
            map.put("msg", StringConstants.EMPTY_PASSWORD);
            return map;
        }
        // 用户名不存在
        User user = userDAO.selectByName(username);
        if (user == null) {
            map.put("msg", StringConstants.NO_SUCH_USER);
            return map;
        }
        // 密码错误
        if (!WendaUtil.MD5(password + user.getSalt()).equals(user.getPassword())) {
            map.put("msg", StringConstants.WRONG_PASSWORD);
            return map;
        }
        // 成功登录
        // 为该user分发一个ticket
        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);

        return map;

    }

    // 为user添加一个ticket
    private String addLoginTicket(int userId) {
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(userId);
        ticket.setStatus(0);// 0表示有效的ticket
        // 生成一个随机的ticket，因为UUID生成的字符串中含有'-'，所以将'-'置换为""
        ticket.setTicket(UUID.randomUUID().toString().replaceAll("-", ""));
        Date date = new Date();
        date.setTime(date.getTime() + 1000 * 3600 * 24 * 100); // 设置有效期为100天
        ticket.setExpired(date);
        loginTicketDAO.addTicket(ticket);
        return ticket.getTicket();
    }

    // 登出功能，将ticket失效即可
    public void logout(String ticket) {
        loginTicketDAO.updateStatus(ticket, 1);
    }

    public User getUser(int id) {
        return userDAO.selectById(id);
    }

    public User getUserByName(String name) {
        return userDAO.selectByName(name);
    }

}
