package me.tianxing.service;

import me.tianxing.dao.MessageDAO;
import me.tianxing.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * Created by TX on 2016/7/29.
 */
@Service
public class MessageService {
    @Autowired
    MessageDAO messageDAO;
    @Autowired
    SensitiveService sensitiveService;

    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveService.filter(message.getContent()));
        return messageDAO.addMessage(message);
    }

    public List<Message> getConversationList(int userId, int offset, int count) {
        return messageDAO.getConversationList(userId, offset, count);
    }

    public List<Message> getConversationDetail(String conversationId, int offset, int count) {
        return messageDAO.getConversationDetail(conversationId, offset, count);
    }

    public int getConversationUnreadCount(int userId, String conversationId) {
        return messageDAO.getConversationUnreadCount(userId, conversationId);
    }

}
