package org.fengfei.lanproxy.server.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author zafir.zhong
 * @description
 * @date Created in 2:06 下午 2022/4/8.
 */
public class IpUtil {

    public static String getLocalIp(){
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        }catch (UnknownHostException e){
            throw new RuntimeException(e);
        }
    }

}
