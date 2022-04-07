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
    public static final String CLIENT_REDIS_KEY = "client_config";
    public static final String INET_REDIS_KEY = "client_inet";
    public static final String USER_PORT_REDIS_KEY = "client_port";
    public static final String LAN_REDIS_KEY = "client_lan";

    public static final int DEFAULT_TIME = 7200;

    public static final int CONFIG_DEFAULT_TIME = 7200*24;


}
