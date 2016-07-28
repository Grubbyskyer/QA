package me.tianxing.constants;

/**
 * Created by TX on 2016/7/22.
 */
public interface StringConstants {
    // 网站的主地址
    String DOMAIN_NAME = "http://localhost:8080";
    // 功能尚在开发中
    String FUNCTION_NOT_COMPLETED = "功能尚在开发中...";

    // geetest的id和key
    String CAPTCHA_ID = "9adf0d24b6611566c0b84aa83b368322";
    String PRIVATE_KEY = "9f04cf49f36d42bbe42d328d003ed5fe";

    // 登录注册界面显示的字符串常量
    String EMPTY_USERNAME = "用户名不能为空！";
    String EMPTY_PASSWORD = "密码不能为空！";
    String USERNAME_ALREADY_EXISTS = "用户名已经被注册！";
    String NO_SUCH_USER = "用户名不存在！";
    String WRONG_PASSWORD = "密码错误，请重新输入！";
    String SERVER_ERROR = "服务器错误！";
    String TEST_CODE_SERVER_ERROR = "验证码服务器错误！";
    String WRONG_TEST_CODE = "验证码错误！";

}
