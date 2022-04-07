package org.fengfei.lanproxy.server.utils;

import org.fengfei.lanproxy.server.config.ProxyConfig;
import org.fengfei.lanproxy.server.entity.Client;

import java.util.*;

/**
 * @author zafir.zhong
 * @description
 * @date Created in 10:39 上午 2022/4/7.
 */
public class ProxyUtils {


    /** 配置变化监听器 */
    private static List<ProxyConfig.ConfigChangedListener> configChangedListeners = new ArrayList<ProxyConfig.ConfigChangedListener>();




    /**
     * 配置更新通知
     */
    public static void notifyconfigChangedListeners(boolean send2Redis) {
        if(send2Redis) {
            RedisUtils.publish("test");
        }
        for (ProxyConfig.ConfigChangedListener changedListener : configChangedListeners) {
            changedListener.onChanged();
        }
    }

    /**
     * 添加配置变化监听器
     *
     * @param configChangedListener
     */
    public static void addConfigChangedListener(ProxyConfig.ConfigChangedListener configChangedListener) {
        configChangedListeners.add(configChangedListener);
    }

    /**
     * 移除配置变化监听器
     *
     * @param configChangedListener
     */
    public static void removeConfigChangedListener(ProxyConfig.ConfigChangedListener configChangedListener) {
        configChangedListeners.remove(configChangedListener);
    }



}
