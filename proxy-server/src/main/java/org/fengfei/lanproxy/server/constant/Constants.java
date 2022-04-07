package org.fengfei.lanproxy.server.constant;

/**
 * @author zafir.zhong
 * @description
 * @date Created in 4:40 下午 2022/4/2.
 */
public class Constants {

    public static final String SERVICE= "LAN_PROXY";
    public static final String PRE_KEY = SERVICE+":";
    public static final String CHANNEL = PRE_KEY + "CHANNEL:CHANGE";
    public static final String SINGLE = "SINGLE";
    public static final String CLUSTER = "CLUSTER";
    public static final String SENTINEL = "SENTINEL";
    public static final String TOKEN = "ALL_SERVICE:TOKEN:";

    public static final int DEFAULT_TIME = 7200;


}
