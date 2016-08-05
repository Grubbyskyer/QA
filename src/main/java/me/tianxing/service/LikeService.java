package me.tianxing.service;

import me.tianxing.constants.StringConstants;
import me.tianxing.util.JedisAdapter;
import me.tianxing.util.RedisKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by TX on 2016/8/3.
 */
@Service
public class LikeService {


    @Autowired
    JedisAdapter jedisAdapter;

    public long like(int userId, int entityType, int entityId) {
        // add to the like list of the specified entity
        String likeKey = RedisKeyGenerator.getLikeKey(entityType, entityId);
        jedisAdapter.sadd(likeKey, String.valueOf(userId));
        // remove from the dislike list of the specified entity
        String dislikeKey = RedisKeyGenerator.getDislikeKey(entityType, entityId);
        jedisAdapter.srem(dislikeKey, String.valueOf(userId));
        // return the count of likes
        return jedisAdapter.scard(likeKey);
    }

    public long dislike(int userId, int entityType, int entityId) {
        // add to the dislike list of the specified entity
        String dislikeKey = RedisKeyGenerator.getDislikeKey(entityType, entityId);
        jedisAdapter.sadd(dislikeKey, String.valueOf(userId));
        // add to the like list of the specified entity
        String likeKey = RedisKeyGenerator.getLikeKey(entityType, entityId);
        jedisAdapter.srem(likeKey, String.valueOf(userId));
        // also return the count of likes
        return jedisAdapter.scard(likeKey);
    }

    public long getLikeCount(int entityType, int entityId) {
        String likeKey = RedisKeyGenerator.getLikeKey(entityType, entityId);
        return jedisAdapter.scard(likeKey);
    }

    public int getLikeStatus(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyGenerator.getLikeKey(entityType, entityId);
        if (jedisAdapter.sismember(likeKey, String.valueOf(userId)))
            return 1;
        String dislikeKey = RedisKeyGenerator.getDislikeKey(entityType, entityId);
        return jedisAdapter.sismember(dislikeKey, String.valueOf(userId)) ? -1 : 0;
    }

}
