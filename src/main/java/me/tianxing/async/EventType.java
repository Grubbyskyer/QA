package me.tianxing.async;

/**
 * Created by TX on 2016/8/5.
 */
public enum EventType {
    LIKE(0),
    COMMENT(1),
    LOGIN(2),
    MAIL(3);

    private int value;

    // the constructor of enum cant be public or protected
    EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
