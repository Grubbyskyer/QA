package me.tianxing.service;

import me.tianxing.dao.FeedDAO;
import me.tianxing.model.Feed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by TX on 2016/8/28.
 */
@Service
public class FeedService {

    @Autowired
    FeedDAO feedDAO;

    public boolean addFeed(Feed feed) {
        feedDAO.addFeed(feed);
        return feed.getId() > 0;
    }

    public Feed getFeedById(int id) {
        return feedDAO.getFeedById(id);
    }

    public List<Feed> getUserFeeds(int maxId, List<Integer> userIds, int count) {
        return feedDAO.selectUserFeeds(maxId, userIds, count);
    }

}
