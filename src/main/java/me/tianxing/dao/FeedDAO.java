package me.tianxing.dao;

import me.tianxing.model.Feed;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by TX on 2016/8/28.
 */
@Mapper
public interface FeedDAO {

    String TABLE_NAME = " feed ";
    String INSERT_FIELDS = " user_id, type, created_date, data ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{userId},#{type},#{createdDate},#{data})"})
    int addFeed(Feed feed);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    Feed getFeedById(int id);

    /**
     * 此方法用XML进行配置，用于拉模式的Feed内容读取
     * @param maxId
     * 指定拉取id < maxId的Feed
     * @param userIds
     * 选取关注的对象Id的列表
     * @param count
     * 指定拉取的数目
     * @return
     */
    List<Feed> selectUserFeeds(@Param("maxId") int maxId,
                               @Param("userIds") List<Integer> userIds,
                               @Param("count") int count);

}
