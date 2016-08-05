package me.tianxing.service;

import me.tianxing.dao.CommentDAO;
import me.tianxing.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * Created by TX on 2016/7/29.
 */
@Service
public class CommentService {

    @Autowired
    CommentDAO commentDAO;


    public List<Comment> getCommentsByEntity(int entityId, int entityType) {
        return commentDAO.selectCommentsByEntity(entityId, entityType);
    }

    public int addComment(Comment comment) {
        return commentDAO.addComment(comment) > 0 ? comment.getId() : 0;
    }

    public int getCommentCount(int entityId, int entityType) {
        return commentDAO.getCommentCount(entityId, entityType);
    }

    public boolean deleteComment(int entityId, int entityType) {
        return commentDAO.updateStatus(entityId, entityType, 1) > 0;
    }

    public Comment getCommentById(int commentId) {
        return commentDAO.getCommentById(commentId);
    }

}
