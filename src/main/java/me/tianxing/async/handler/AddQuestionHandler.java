package me.tianxing.async.handler;

import me.tianxing.async.EventHandler;
import me.tianxing.async.EventModel;
import me.tianxing.async.EventType;
import me.tianxing.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Created by TX on 2016/9/3.
 */
@Component
public class AddQuestionHandler implements EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(AddQuestionHandler.class);
    @Autowired
    SearchService searchService;

    @Override
    public void doHandle(EventModel eventModel) {
        try {
            searchService.indexQuestion(eventModel.getEntityId(),
                    eventModel.getExt("title"), eventModel.getExt("content"));
        } catch (Exception e) {
            logger.error("增加题目索引失败");
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.ADD_QUESTION);
    }
}

