package me.tianxing.service;

import me.tianxing.util.JedisAdapter;
import me.tianxing.util.RedisKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by TX on 2016/8/20.
 */
@Service
public class FollowService {

    @Autowired
    JedisAdapter jedisAdapter;

    /**
     * 某个用户对某个实体点击了关注按钮，则：
     * 1. 将该用户添加到该实体的粉丝列表中去
     * 2. 将该实体添加到该用户的关注对象中去
     * 用redis事务实现
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean follow(int userId, int entityType, int entityId) {
        String followerKey = RedisKeyGenerator.getFollowerKey(entityType, entityId);
        String followeeKey = RedisKeyGenerator.getFolloweeKey(userId, entityType);
        Date date = new Date();
        Jedis jedis = jedisAdapter.getJedis();
        Transaction tx = jedisAdapter.multi(jedis);
        // 将该用户添加到该实体的粉丝列表中去
        tx.zadd(followerKey, date.getTime(), String.valueOf(userId));
        // 将该实体添加到此用户对应实体类型的关注对象列表中去
        tx.zadd(followeeKey, date.getTime(), String.valueOf(entityId));
        List<Object> res = jedisAdapter.exec(tx, jedis);
        return res.size() == 2 && (Long) res.get(0) > 0 && (Long) res.get(1) > 0;
    }

    /**
     * 取消关注，与上面的刚好相反
     * 同样也用redis事务实现
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean unfollow(int userId, int entityType, int entityId) {
        String followerKey = RedisKeyGenerator.getFollowerKey(entityType, entityId);
        String followeeKey = RedisKeyGenerator.getFolloweeKey(userId, entityType);
        Jedis jedis = jedisAdapter.getJedis();
        Transaction tx = jedisAdapter.multi(jedis);
        // 将该用户从该实体的粉丝列表中删除
        tx.zrem(followerKey, String.valueOf(userId));
        // 将该实体从此用户对应实体类型的关注对象列表中删除
        tx.zrem(followeeKey, String.valueOf(entityId));
        List<Object> res = jedisAdapter.exec(tx, jedis);
        return res.size() == 2 && (Long) res.get(0) > 0 && (Long) res.get(1) > 0;
    }

    /**
     * 判断某个用户是不是某个实体的粉丝，亦即他是否关注了这个对象
     * sorted set没有类似sismember这样的方法，直接使用zscore判断，
     * 如果不包含指定的member，则返回null
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean isFollower(int userId, int entityType, int entityId) {
        String followerKey = RedisKeyGenerator.getFollowerKey(entityType, entityId);
        return jedisAdapter.zscore(followerKey, String.valueOf(userId)) != null;
    }

    /**
     * 获取某个对象的粉丝数，亦即有多少人关注了它
     * @param entityType
     * @param entityId
     * @return
     */
    public long getFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyGenerator.getFollowerKey(entityType, entityId);
        return jedisAdapter.zcard(followerKey);
    }

    /**
     * 获取某个用户对某个实体类型的关注对象数
     * @param userId
     * @param entityType
     * @return
     */
    public long getFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyGenerator.getFolloweeKey(userId, entityType);
        return jedisAdapter.zcard(followeeKey);
    }

    /**
     * 一个Helper函数，将从jedis中取出来的Set\<String\>转为List\<Integer\>
     * @param idset
     * @return
     */
    private List<Integer> getIdsFromSet(Set<String> idset) {
        List<Integer> idlist = new ArrayList<Integer>();
        for (String id : idset) {
            idlist.add(Integer.parseInt(id));
        }
        return idlist;
    }

    /**
     * 获取某个实体的粉丝，因为这里是按照时间逆序排列的，所以用zrevrange
     * @param entityType
     * @param entityId
     * @param count
     * @return
     */
    public List<Integer> getFollowers(int entityType, int entityId, int count) {
        String followerKey = RedisKeyGenerator.getFollowerKey(entityType, entityId);
        return getIdsFromSet(jedisAdapter.zrevrange(followerKey, 0, count));
    }

    /**
     * 带offset的重载，用于分页，再次注意：
     * zrevrange()的后面两个参数是start和end，而mysql中limit的两个参数是offset和count
     * @param entityType
     * @param entityId
     * @param offset
     * @param count
     * @return
     */
    public List<Integer> getFollowers(int entityType, int entityId, int offset, int count) {
        String followerKey = RedisKeyGenerator.getFollowerKey(entityType, entityId);
        return getIdsFromSet(jedisAdapter.zrevrange(followerKey, offset, offset + count));
    }

    public List<Integer> getFollowees(int userId, int entityType, int count) {
        String followeeKey = RedisKeyGenerator.getFolloweeKey(userId, entityType);
        return getIdsFromSet(jedisAdapter.zrevrange(followeeKey, 0, count));
    }

    public List<Integer> getFollowees(int userId, int entityType, int offset, int count) {
        String followeeKey = RedisKeyGenerator.getFolloweeKey(userId, entityType);
        return getIdsFromSet(jedisAdapter.zrevrange(followeeKey, offset, offset + count));
    }

}
