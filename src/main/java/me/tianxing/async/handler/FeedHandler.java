package me.tianxing.async.handler;

import com.alibaba.fastjson.JSONObject;
import me.tianxing.async.EventHandler;
import me.tianxing.async.EventModel;
import me.tianxing.async.EventType;
import me.tianxing.model.EntityType;
import me.tianxing.model.Feed;
import me.tianxing.model.Question;
import me.tianxing.model.User;
import me.tianxing.service.FeedService;
import me.tianxing.service.FollowService;
import me.tianxing.service.QuestionService;
import me.tianxing.service.UserService;
import me.tianxing.util.JedisAdapter;
import me.tianxing.util.RedisKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by TX on 2016/8/28.
 */
@Component
public class FeedHandler implements EventHandler {

    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    @Autowired
    FeedService feedService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    QuestionService questionService;

    /**
     * 一个辅助方法
     *
     * @param eventModel
     * @return
     */
    private String buildFeedData(EventModel eventModel) {
        Map<String, String> map = new HashMap<String, String>();
        // 触发用户是通用的
        User actor = userService.getUser(eventModel.getActorId());
        if (actor == null) {
            return null;
        }
        map.put("userId", String.valueOf(actor.getId()));
        map.put("userHead", actor.getHeadUrl());
        map.put("userName", actor.getName());

        if (eventModel.getType() == EventType.COMMENT ||
                (eventModel.getType() == EventType.FOLLOW && eventModel.getEntityType() == EntityType.ENTITY_QUESTION)) {
            Question question = questionService.selectById(eventModel.getEntityId());
            if (question == null) {
                return null;
            }
            map.put("questionId", String.valueOf(question.getId()));
            map.put("questionTitle", question.getTitle());
            return JSONObject.toJSONString(map);
        }
        return null;
    }

    @Override
    public void doHandle(EventModel eventModel) {
        // 构造一个新鲜事
        Feed feed = new Feed();
        feed.setCreatedDate(new Date());
        feed.setType(eventModel.getType().getValue());
        feed.setUserId(eventModel.getActorId());
        feed.setData(buildFeedData(eventModel));
        if (feed.getData() == null) {
            // 不支持的feed
            return;
        }
        // 添加到数据库中，拉模式已经可以直接读取了，推模式还需要将该feedID加到各个粉丝的timeline中
        feedService.addFeed(feed);

        // 获得所有粉丝
        List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER, eventModel.getActorId(), Integer.MAX_VALUE);
        // 系统队列
        followers.add(0);
        // 给所有粉丝推事件
        for (int follower : followers) {
            String timelineKey = RedisKeyGenerator.getTimelineKey(follower);
            jedisAdapter.lpush(timelineKey, String.valueOf(feed.getId()));
            // 限制最长长度，如果timelineKey的长度过大，就删除后面的新鲜事
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(new EventType[]{EventType.COMMENT, EventType.FOLLOW});
    }
}
