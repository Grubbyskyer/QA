package me.tianxing.async;

import com.alibaba.fastjson.JSONObject;
import me.tianxing.util.JedisAdapter;
import me.tianxing.util.RedisKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javafx.scene.input.KeyCode.L;

/**
 * Created by TX on 2016/8/5.
 */
@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private ApplicationContext applicationContext;

    // config stores the map from EventType to EventHandler(List)
    private Map<EventType, List<EventHandler>> config = new HashMap<EventType, List<EventHandler>>();

    @Autowired
    JedisAdapter jedisAdapter;

    @Override
    public void afterPropertiesSet() throws Exception {
        // get all the handlers that implements the EventHanler interface
        Map<String, EventHandler> handlers = applicationContext.getBeansOfType(EventHandler.class);
        if (handlers != null) {
            // traverse through the handlers, find the EventTypes that each handler support
            for (Map.Entry<String, EventHandler> entry : handlers.entrySet()) {
                // get the EventTypes that each handler support, note : its a list
                List<EventType> types = entry.getValue().getSupportEventTypes();
                for (EventType type : types) {
                    // if this type hasn't been put to the config, first put in an empty ArrayList
                    if (!config.containsKey(type)) {
                        config.put(type, new ArrayList<EventHandler>());
                    }
                    // put the handler to the config map
                    config.get(type).add(entry.getValue());
                }
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // get todoo events from the blockingqueue
                    String key = RedisKeyGenerator.getEventQueueKey();
                    List<String> events = jedisAdapter.brpop(0, key);

                    for (String eventModelString : events) {
                        /*  brpop will first return the value of the key (see BLPOP)
                            redis> DEL list1 list2
                            (integer) 0
                            redis> RPUSH list1 a b c
                            (integer) 3
                            redis> BLPOP list1 list2 0
                            1) "list1" // first returns "list1"
                            2) "a"
                         */
                        if (eventModelString.equals(key)) {
                            continue;
                        }
                        // parse the json string to EventModel
                        EventModel eventModel = JSONObject.parseObject(eventModelString, EventModel.class);
                        // if this EventType hasn't been registered in the config, continue;
                        if (!config.containsKey(eventModel.getType())) {
                            logger.error("不能识别的事件！");
                            continue;
                        }
                        // else handle this event(maybe multiple handlers)
                        for (EventHandler eventHandler : config.get(eventModel.getType())) {
                            eventHandler.doHandle(eventModel);
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
