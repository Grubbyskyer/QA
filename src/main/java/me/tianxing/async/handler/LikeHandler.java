package me.tianxing.async.handler;

import me.tianxing.async.EventHandler;
import me.tianxing.async.EventModel;
import me.tianxing.async.EventType;
import me.tianxing.model.Message;
import me.tianxing.model.User;
import me.tianxing.service.MessageService;
import me.tianxing.service.UserService;
import me.tianxing.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by TX on 2016/8/5.
 */
@Component
public class LikeHandler implements EventHandler {
    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;

    @Override
    public void doHandle(EventModel eventModel) {
        // send a message
        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(eventModel.getEntityOwnerId());
        message.setCreatedDate(new Date());
        message.setHasRead(0);
        User user = userService.getUser(eventModel.getActorId());
        message.setContent("用户" + user.getName()
                + "赞了你的评论, 点击查看： http://localhost:8080/question/" + eventModel.getExt("questionId"));
        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        // register the EventType this handler need to handle
        return Arrays.asList(EventType.LIKE);
    }
}
