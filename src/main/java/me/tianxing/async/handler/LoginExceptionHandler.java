package me.tianxing.async.handler;

import me.tianxing.async.EventHandler;
import me.tianxing.async.EventModel;
import me.tianxing.async.EventType;
import me.tianxing.util.MailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by TX on 2016/8/5.
 */
@Component
public class LoginExceptionHandler implements EventHandler {
    @Autowired
    MailSender mailSender;

    @Override
    public void doHandle(EventModel eventModel) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", eventModel.getExt("username"));
        mailSender.sendWithHTMLTemplate(eventModel.getExt("email"),
                "登录IP异常", "mails/login_exception.html", map);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LOGIN);
    }
}
