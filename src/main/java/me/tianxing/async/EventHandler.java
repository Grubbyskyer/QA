package me.tianxing.async;

import java.util.List;

/**
 * Created by TX on 2016/8/5.
 */
public interface EventHandler {
    void doHandle(EventModel eventModel);

    List<EventType> getSupportEventTypes();
}
