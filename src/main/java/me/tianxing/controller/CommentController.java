package me.tianxing.controller;

import me.tianxing.model.Comment;
import me.tianxing.model.EntityType;
import me.tianxing.model.HostHolder;
import me.tianxing.model.Question;
import me.tianxing.service.CommentService;
import me.tianxing.service.QuestionService;
import me.tianxing.service.SensitiveService;
import me.tianxing.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;


/**
 * Created by TX on 2016/7/29.
 */
@Controller
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    CommentService commentService;

    @Autowired
    SensitiveService sensitiveService;

    @Autowired
    QuestionService questionService;

    @Autowired
    HostHolder hostHolder;

    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})
    public String addComment(@RequestParam("questionId") int questionId,
                             @RequestParam("content") String content) {
        try {
            // filter the content
            content = HtmlUtils.htmlEscape(content);
            content = sensitiveService.filter(content);
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setCreatedDate(new Date());
            comment.setEntityId(questionId);
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            comment.setStatus(0);
            // if no user found, mark it as anonymous user
            if (hostHolder.getUser() == null) {
                comment.setUserId(WendaUtil.ANONYMOUS_USERID);
            } else {
                comment.setUserId(hostHolder.getUser().getId());
            }
            commentService.addComment(comment);
            // update the comment_count in table question
            int commentCount = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            questionService.updateCommentCount(comment.getEntityId(), commentCount);
        } catch (Exception e) {
            logger.error("增加评论失败！" + e.getMessage());
        }
        return "redirect:/question/" + String.valueOf(questionId);
    }

}
