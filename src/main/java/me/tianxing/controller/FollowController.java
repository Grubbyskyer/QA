package me.tianxing.controller;

import me.tianxing.async.EventModel;
import me.tianxing.async.EventProducer;
import me.tianxing.async.EventType;
import me.tianxing.model.*;
import me.tianxing.service.CommentService;
import me.tianxing.service.FollowService;
import me.tianxing.service.QuestionService;
import me.tianxing.service.UserService;
import me.tianxing.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by TX on 2016/8/21.
 */
@Controller
public class FollowController {

    private static final Logger logger = LoggerFactory.getLogger(FollowController.class);

    @Autowired
    HostHolder hostHolder;

    @Autowired
    FollowService followService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    /**
     * 用户点击关注某个用户
     * 如果是未登录状态，返回999，由前端控制跳转到登录界面
     * 否则就执行关注的事务，并将关注之后的关注对象总数返回
     * @param userId
     * @return
     */
    @RequestMapping(path = {"/followUser"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followUser(@RequestParam("userId") int userId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }
        int localUserId = hostHolder.getUser().getId();
        boolean res = followService.follow(localUserId, EntityType.ENTITY_USER, userId);
        // 发送一个私信
        if (res) {
            eventProducer.fireEvent(new EventModel(EventType.FOLLOW).setActorId(localUserId)
                    .setEntityType(EntityType.ENTITY_USER).setEntityId(userId).setEntityOwnerId(userId));
        }
        return WendaUtil.getJSONString(res ? 0 : 1,
                String.valueOf(followService.getFolloweeCount(localUserId, EntityType.ENTITY_USER)));
    }

    /**
     * 取消关注某个用户
     * @param userId
     * @return
     */
    @RequestMapping(path = {"/unfollowUser"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowUser(@RequestParam("userId") int userId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }
        int localUserId = hostHolder.getUser().getId();
        boolean res = followService.unfollow(localUserId, EntityType.ENTITY_USER, userId);
        // 发送一个私信，取消关注是否发送私信？ 这里先不发送，没有实现UnfollowHandler
//        if (res) {
//            eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW).setActorId(localUserId)
//                    .setEntityType(EntityType.ENTITY_USER).setEntityId(userId).setEntityOwnerId(userId));
//        }
        return WendaUtil.getJSONString(res ? 0 : 1,
                String.valueOf(followService.getFolloweeCount(localUserId, EntityType.ENTITY_USER)));
    }

    /**
     * 关注一个问题
     * 首先判断是否登录，以及问题是否存在
     * 然后关注此问题，并发送站内信
     * 将此用户的信息也返回，显示在此问题的界面上
     * @param questionId
     * @return
     */
    @RequestMapping(path = {"/followQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followQuestion(@RequestParam("questionId") int questionId) {
        User localUser;
        if ((localUser = hostHolder.getUser()) == null) {
            return WendaUtil.getJSONString(999);
        }
        Question question = questionService.selectById(questionId);
        if (question == null) {
            return WendaUtil.getJSONString(1, "问题不存在！");
        }
        boolean res = followService.follow(localUser.getId(), EntityType.ENTITY_QUESTION, questionId);
        if (res) {
            eventProducer.fireEvent(new EventModel(EventType.FOLLOW).setActorId(localUser.getId())
                    .setEntityType(EntityType.ENTITY_QUESTION).setEntityId(questionId)
                    .setEntityOwnerId(question.getUserId()));
        }
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("headUrl", localUser.getHeadUrl());
        info.put("name", localUser.getName());
        info.put("id", localUser.getId());
        info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION, questionId));
        return WendaUtil.getJSONString(res ? 0 : 1, info);
    }

    /**
     * 取消关注一个问题
     * @param questionId
     * @return
     */
    @RequestMapping(path = {"/unfollowQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowQuestion(@RequestParam("questionId") int questionId) {
        User localUser;
        if ((localUser = hostHolder.getUser()) == null) {
            return WendaUtil.getJSONString(999);
        }
        Question question = questionService.selectById(questionId);
        if (question == null) {
            return WendaUtil.getJSONString(1, "问题不存在！");
        }
        boolean res = followService.unfollow(localUser.getId(), EntityType.ENTITY_QUESTION, questionId);
        if (res) {
            eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW).setActorId(localUser.getId())
                    .setEntityType(EntityType.ENTITY_QUESTION).setEntityId(questionId)
                    .setEntityOwnerId(question.getUserId()));
        }
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("id", localUser.getId());
        info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION, questionId));
        return WendaUtil.getJSONString(res ? 0 : 1, info);
    }

    /**
     * 获取某个用户的粉丝列表
     * @param model
     * @param userId
     * @return
     */
    @RequestMapping(path = {"/user/{uid}/followers"}, method = {RequestMethod.GET})
    public String followers(Model model, @PathVariable("uid") int userId) {
        List<Integer> followerIds = followService.getFollowers(EntityType.ENTITY_USER, userId, 0, 10);
        if (hostHolder.getUser() == null) {
            model.addAttribute("followers", getUsersInfo(0, followerIds));
        } else {
            model.addAttribute("followers", getUsersInfo(hostHolder.getUser().getId(), followerIds));
        }
        model.addAttribute("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        model.addAttribute("curUser", userService.getUser(userId));
        return "followers";
    }

    /**
     * 获取某个用户关注了哪些人
     * @param model
     * @param userId
     * @return
     */
    @RequestMapping(path = {"/user/{uid}/followees"}, method = {RequestMethod.GET})
    public String followees(Model model, @PathVariable("uid") int userId) {
        List<Integer> followeeIds = followService.getFollowees(userId, EntityType.ENTITY_USER, 0, 10);
        if (hostHolder.getUser() == null) {
            model.addAttribute("followees", getUsersInfo(0, followeeIds));
        } else {
            model.addAttribute("followees", getUsersInfo(hostHolder.getUser().getId(), followeeIds));
        }
        model.addAttribute("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        model.addAttribute("curUser", userService.getUser(userId));
        return "followees";
    }

    /**
     * 一个辅助函数
     * @param localUserId
     * 当前登录用户的ID，用来显示“关注”按钮是否可用
     * @param userIds
     * 粉丝或者关注对象的ID列表
     * @return
     */
    private List<ViewObject> getUsersInfo(int localUserId, List<Integer> userIds) {
        List<ViewObject> usersInfo = new ArrayList<ViewObject>();
        for (Integer userId : userIds) {
            User user = userService.getUser(userId);
            if (user == null) {
                continue;
            }
            /**
             * 把界面列表中每个人的信息都显示出来
             * 包括评论总数，粉丝数，关注对象数
             * 以及当前登录用户是否关注了此人，如果当前未登录，那么在上面的两个方法中会传入一个localUserId = 0，
             * 默认为未关注，亦即可以点击关注按钮
             */
            ViewObject vo = new ViewObject();
            vo.set("user", user);
            vo.set("commentCount", commentService.getUserCommentCount(userId));
            vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
            vo.set("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
            if (localUserId == 0) {
                vo.set("followed", false);
            } else {
                vo.set("followed", followService.isFollower(localUserId, EntityType.ENTITY_USER, userId));
            }
            usersInfo.add(vo);
        }
        return usersInfo;
    }

}
