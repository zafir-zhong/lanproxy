package org.fengfei.lanproxy.server.entity;

/**
 * @author zafir.zhong
 * @description
 * @date Created in 2:11 下午 2022/4/8.
 */
public class ChannelSetting {

    private final int CONNECTED = 0;

    private final int AUTH_SUCCESS = 1;


    private int status;

    private String ip;


    public ChannelSetting(int status, String ip) {
        this.status = status;
        this.ip = ip;
    }

    public ChannelSetting() {
    }
}
