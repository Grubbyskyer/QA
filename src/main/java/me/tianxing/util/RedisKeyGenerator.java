package me.tianxing.util;


/**
 * Created by TX on 2016/8/3.
 */
public class RedisKeyGenerator {
    private static final String SPLIT = ":";
    private static final String BIZ_LIKE = "LIKE";
    private static final String BIZ_DISLIKE = "DISLIKE";
    private static final String BIZ_EVENT_QUEUE = "EVENT_QUEUE";

    // follower是指粉丝，指某个实体的粉丝（有哪些用户关注了这个实体）
    private static final String BIZ_FOLLOWER = "FOLLOWER";

    // followee是指关注对象，指某个用户关注了哪些实体
    private static final String BIZ_FOLLOWEE = "FOLLOWEE";

    // 新鲜事的列表
    private static final String BIZ_TIMELINE = "TIMELINE";

    public static String getLikeKey(int entityType, int entityId) {
        return BIZ_LIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    public static String getDislikeKey(int entityType, int entityId) {
        return BIZ_DISLIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    public static String getEventQueueKey() {
        return BIZ_EVENT_QUEUE;
    }

    /**
     * 返回某个实体的粉丝 列表的key
     * @param entityType 指的是实体类型，比如Question，User
     * @param entityId 指的是在该实体类型下的编号
     *        由 type + id 可以唯一确定一个实体
     * @return
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return BIZ_FOLLOWER + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    /**
     * 返回某个用户在某个实体类型上的所有关注对象 列表的key
     * 比如userId = 1, entityType = User, 那么就是返回用户1关注的所有人
     * @param userId 用户Id
     * @param entityType 指的是实体类型，比如Question，User
     * @return
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return BIZ_FOLLOWEE + SPLIT + String.valueOf(userId) + SPLIT + String.valueOf(entityType);
    }

    public static String getTimelineKey(int userId) {
        return BIZ_TIMELINE + SPLIT + String.valueOf(userId);
    }

}
