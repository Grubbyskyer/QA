package me.tianxing.model;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

/**
 * Created by TX on 2016/8/28.
 */
public class Feed {

    private int id;
    private int type;
    private int userId;
    private Date createdDate;
    private String data;
    // 为了模板中取值方便，多设置一个JSON字段
    private JSONObject dataJSON = null;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
        dataJSON = JSONObject.parseObject(data);
    }
    // 为了Velocity中取值方便，直接使用get方法
    public String get(String key) {
        return dataJSON == null ? null : dataJSON.getString(key);
    }

}
