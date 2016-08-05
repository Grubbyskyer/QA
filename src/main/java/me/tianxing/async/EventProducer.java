package me.tianxing.async;

import com.alibaba.fastjson.JSONObject;
import me.tianxing.util.JedisAdapter;
import me.tianxing.util.RedisKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by TX on 2016/8/5.
 */
@Service
public class EventProducer {

    private static final Logger logger = LoggerFactory.getLogger(EventProducer.class);

    @Autowired
    JedisAdapter jedisAdapter;

    public boolean fireEvent(EventModel eventModel) {
        try {
            String key = RedisKeyGenerator.getEventQueueKey();
            String json = JSONObject.toJSONString(eventModel);
            jedisAdapter.lpush(key, json);
            return true;
        } catch (Exception e) {
            logger.error("添加事件到消息队列失败！" + e.getMessage());
            return false;
        }
    }

}
