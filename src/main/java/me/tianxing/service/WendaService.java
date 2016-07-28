package me.tianxing.service;

import org.springframework.stereotype.Service;

/**
 * Created by TX on 2016/7/20.
 */
@Service
public class WendaService {

    public String getMessage(int userId) {
        return "Hello Message: " + String.valueOf(userId);
    }

}
