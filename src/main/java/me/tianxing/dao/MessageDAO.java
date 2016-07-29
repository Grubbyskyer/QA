package me.tianxing.dao;

import me.tianxing.model.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by TX on 2016/7/29.
 */
@Mapper
public interface MessageDAO {

    String TABLE_NAME = " message ";
    String INSERT_FIELDS = " from_id, to_id, content, created_date, has_read, conversation_id ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{fromId},#{toId},#{content},#{createdDate},#{hasRead},#{conversationId})"})
    int addMessage(Message message);

    @Select({"select ", SELECT_FIELDS, "from ", TABLE_NAME, " where conversation_id=#{conversationId} order by id desc limit #{offset}, #{count}"})
    List<Message> getConversationDetail(@Param("conversationId") String conversationId,
                                        @Param("offset") int offset, @Param("count") int count);

    // use id to store the value of count
    @Select({"select ", INSERT_FIELDS, " ,count(id) as id from ( select * from ", TABLE_NAME, " where from_id=#{userId} or to_id=#{userId} order by id desc) tt group by conversation_id  order by created_date desc limit #{offset}, #{count}"})
    List<Message> getConversationList(@Param("userId") int userId,
                                        @Param("offset") int offset, @Param("count") int count);

    @Select({"select count(id) from ", TABLE_NAME, " where has_read=0 and to_id=#{toId} and conversation_id=#{conversationId}"})
    int getConversationUnreadCount(@Param("toId") int toId, @Param("conversationId") String conversationId);

}
