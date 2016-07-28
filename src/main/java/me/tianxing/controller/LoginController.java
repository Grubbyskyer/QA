package me.tianxing.controller;

import me.tianxing.constants.StringConstants;
import me.tianxing.service.UserService;
import me.tianxing.util.GeetestLib;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by TX on 2016/7/22.
 */
@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserService userService;

    // 提交注册的数据
    @RequestMapping(path = {"/regpost/"}, method = {RequestMethod.POST})
    public String regpost(Model model, @RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam(value = "callback", required = false) String callback,
                           @RequestParam("geetest_challenge") String challenge,
                           @RequestParam("geetest_validate") String validate,
                           @RequestParam("geetest_seccode") String seccode,
                           HttpServletRequest httpServletRequest,
                           HttpServletResponse response) {
        int gtResult = 0;
        try {
            int gtcode = (Integer) httpServletRequest.getSession().getAttribute("gt_server_status");
            String gtuserid = (String) httpServletRequest.getSession().getAttribute("gt_user_id");
            gtResult = userService.geetest(challenge, validate, seccode, gtcode, gtuserid);
        } catch (Exception e) {
            logger.error("注册验证码异常！" + e.getMessage());
            model.addAttribute("msg", StringConstants.TEST_CODE_SERVER_ERROR);
            return "txregister";
        }
        if (gtResult != 1) {
            model.addAttribute("msg", StringConstants.WRONG_TEST_CODE);
            return "txregister";
        }
        try {
            Map<String, String> map = userService.register(username, password);
            // map包含ticket或者msg
            // 注册成功情况下包含"ticket"子段
            if (map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket", map.get("ticket"));
                cookie.setPath("/");
                // 注册完默认为添加cookie，那么为其设置cookie的存活时间
                cookie.setMaxAge(3600 * 24 * 5); // 设置存活5天 默认值为-1表示关闭浏览器就消失
                // 为浏览器添加cookie
                response.addCookie(cookie);
                // 如果有回调URL，判断该URL是不是一个站内地址，如果是则返回该URL
                if (StringUtils.isNotBlank(callback) && callback.startsWith(StringConstants.DOMAIN_NAME)) {
                    return "redirect:" + callback;
                }
                // 若没有callback，或callback非法，则直接返回主页
                return "redirect:/";
            } else { // 注册失败，返回到登录注册界面，此时包含"msg"子段
                model.addAttribute("msg", map.get("msg"));
                return "txregister";
            }
        } catch (Exception e) {
            logger.error("注册异常！" + e.getMessage());
            model.addAttribute("msg", StringConstants.SERVER_ERROR);
            return "txregister";
        }
    }

    // 提交登录数据
    @RequestMapping(path = {"/loginpost/"}, method = {RequestMethod.POST})
    public String loginpost(Model model, @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam(value = "rememberme", defaultValue = "false") boolean rememberme,
                        @RequestParam(value = "callback", required = false) String callback,
                        @RequestParam("geetest_challenge") String challenge,
                        @RequestParam("geetest_validate") String validate,
                        @RequestParam("geetest_seccode") String seccode,
                        HttpServletRequest httpServletRequest,
                        HttpServletResponse response) {
        int gtResult = 0;
        try {
            int gtcode = (Integer) httpServletRequest.getSession().getAttribute("gt_server_status");
            String gtuserid = (String) httpServletRequest.getSession().getAttribute("gt_user_id");
            gtResult = userService.geetest(challenge, validate, seccode, gtcode, gtuserid);
        } catch (Exception e) {
            logger.error("登录验证码异常！" + e.getMessage());
            model.addAttribute("msg", StringConstants.TEST_CODE_SERVER_ERROR);
            return "txlogin";
        }
        if (gtResult != 1) {
            model.addAttribute("msg", StringConstants.WRONG_TEST_CODE);
            return "txlogin";
        }
        try {
            Map<String, String> map = userService.login(username, password);
            // map包含ticket或者msg
            // 登录成功情况下包含"ticket"子段
            if (map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket", map.get("ticket"));
                cookie.setPath("/");
                if (rememberme) { // 如果点击了<记住我>，那么为其设置cookie的存活时间
                    cookie.setMaxAge(3600 * 24 * 5); // 设置存活5天 默认值为-1表示关闭浏览器就消失
                }
                // 为浏览器添加cookie
                response.addCookie(cookie);
                // 如果有回调URL，判断该URL是不是一个站内地址，如果是则返回该URL
                if (StringUtils.isNotBlank(callback) && callback.startsWith(StringConstants.DOMAIN_NAME)) {
                    return "redirect:" + callback;
                }
                // 若没有callback，或callback非法，则直接返回主页
                return "redirect:/";
            } else { // 登录失败，返回到登录注册界面，此时包含"msg"子段
                model.addAttribute("msg", map.get("msg"));
                return "txlogin";
            }
        } catch (Exception e) {
            logger.error("登录异常！" + e.getMessage());
            model.addAttribute("msg", StringConstants.SERVER_ERROR);
            return "txlogin";
        }
    }

    // 登出功能，直接从cookie中找到ticket，然后将其“删除”（将其status置为1）
    @RequestMapping(path = {"/logout"}, method = {RequestMethod.POST, RequestMethod.GET})
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/";
    }

    // 注册和登录的界面，方法用GET，因为是读取界面而不是提交数据
    // 该方法已过期，请勿使用
//    @RequestMapping(path = {"/reglogin"}, method = {RequestMethod.GET})
//    public String reglogin(Model model, @RequestParam(value = "callback", defaultValue = "", required = false) String callback) {
//        model.addAttribute("callback", callback);
//        return "login";
//    }

    // 初始化验证码
    @RequestMapping(path = {"/initgee"}, method = {RequestMethod.GET})
    @ResponseBody
    public String initGeetest(HttpServletRequest httpServletRequest) {
        GeetestLib geetestLib = new GeetestLib(StringConstants.CAPTCHA_ID, StringConstants.PRIVATE_KEY);
        String resStr = "{}";
        //自定义userid
        String userid = "test";
        //进行验证预处理
        int gtServerStatus = geetestLib.preProcess(userid);
        //将服务器状态设置到session中
        httpServletRequest.getSession().setAttribute(geetestLib.gtServerStatusSessionKey, gtServerStatus);
        //将userid设置到session中
        httpServletRequest.getSession().setAttribute("gt_user_id", userid);
        resStr = geetestLib.getResponseStr();
        return resStr;
    }

    // 注册界面
    @RequestMapping(path = {"/register"}, method = {RequestMethod.GET})
    public String register(Model model, @RequestParam(value = "callback", defaultValue = "", required = false) String callback) {
        model.addAttribute("callback", callback);
        return "txregister";
    }

    // 登录界面
    @RequestMapping(path = {"/login"}, method = {RequestMethod.GET})
    public String login(Model model, @RequestParam(value = "callback", defaultValue = "", required = false) String callback) {
        model.addAttribute("callback", callback);
        return "txlogin";
    }

}
