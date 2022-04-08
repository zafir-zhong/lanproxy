package org.fengfei.lanproxy.server.utils;

import com.alibaba.fastjson.JSONArray;
import org.fengfei.lanproxy.server.constant.Constants;
import org.fengfei.lanproxy.server.entity.Client;
import org.fengfei.lanproxy.server.entity.ClientProxyMapping;

import java.util.*;

/**
 * @author zafir.zhong
 * @description
 * @date Created in 10:48 上午 2022/4/7.
 */
public class ConfigDataFlowUtils {

    public static void updateClients(List<Client> clientsUpdated){
        final String configKey = Constants.PRE_KEY + Constants.CLIENT_REDIS_KEY;
        final String userPortKey = Constants.PRE_KEY + Constants.USER_PORT_REDIS_KEY;
        final String lanKey = Constants.PRE_KEY + Constants.LAN_REDIS_KEY;
        final String inetKey = Constants.PRE_KEY + Constants.INET_REDIS_KEY;

        try {
            RedisUtils.set(configKey, JSONArray.toJSONString(clientsUpdated), Constants.CONFIG_DEFAULT_TIME);
            initInet(clientsUpdated);
            initLanInfo(clientsUpdated);
            initUserPort(clientsUpdated);
            MysqlUtils.updateClients(clientsUpdated);
            ProxyUtils.notifyconfigChangedListeners(true);
        }catch (Exception e){
            RedisUtils.delete(configKey);
            RedisUtils.delete(userPortKey);
            RedisUtils.delete(lanKey);
            RedisUtils.delete(inetKey);
        }
    }

    public static List<Client> getClients(){
        final String key = Constants.PRE_KEY + Constants.CLIENT_REDIS_KEY;
        final String client = RedisUtils.get(key);
        if(client == null || client.isEmpty()){
            List<Client> clients = MysqlUtils.getClients();
            RedisUtils.set(key, JSONArray.toJSONString(clients), Constants.CONFIG_DEFAULT_TIME);
            return clients;
        }
        return JSONArray.parseArray(client, Client.class);
    }

    public static List<Integer> getClientInetPorts(String clientId){
        final String key = Constants.PRE_KEY + Constants.INET_REDIS_KEY;
        final Map<String, String> inet = RedisUtils.hget(key);
        if(inet == null || inet.isEmpty()){
            final Map<String, String> cache = initInet(getClients());
            final String s = cache.get(clientId);
            if(s == null){
                return Collections.emptyList();
            }
            return JSONArray.parseArray(s, Integer.class);
        }
        final String s = inet.get(clientId);
        if(s == null){
            return Collections.emptyList();
        }
        return JSONArray.parseArray(s, Integer.class);
    }

    public static Set<String> getClientKeySet() {
        final String key = Constants.PRE_KEY + Constants.INET_REDIS_KEY;
        Map<String, String> cache = RedisUtils.hget(key);
        if(cache == null || cache.isEmpty()){
            cache = initInet(getClients());
        }
        return cache.keySet();
    }

    public static String getLanInfo(Integer port){
        final String key = Constants.PRE_KEY + Constants.LAN_REDIS_KEY;
        Map<String, String> inet = RedisUtils.hget(key);
        if(inet == null || inet.isEmpty()){
            inet = initLanInfo(getClients());
        }
        return inet.get(String.valueOf(port));
    }

    public static List<Integer> getUserPorts(){
        final String key = Constants.PRE_KEY + Constants.USER_PORT_REDIS_KEY;
        String cache = RedisUtils.get(key);
        if(cache == null || cache.isEmpty()){
            return initUserPort(getClients());
        }
        return JSONArray.parseArray(cache, Integer.class);
    }


    private static Map<String, String> initInet(List<Client> clients){
        final String key = Constants.PRE_KEY + Constants.INET_REDIS_KEY;
        Map<String, String> cache = new HashMap<>();
        for (Client tmpClient : clients) {
            if(tmpClient == null || tmpClient.getClientKey() == null){
                continue;
            }
            if(tmpClient.getProxyMappings() == null || tmpClient.getProxyMappings().isEmpty()){
                cache.put(tmpClient.getClientKey(), JSONArray.toJSONString(Collections.emptyList()));
                continue;
            }
            List<Integer> ports = new ArrayList<>();
            for (ClientProxyMapping proxyMapping : tmpClient.getProxyMappings()) {
                if(proxyMapping != null && proxyMapping.getInetPort() != null){
                    ports.add(proxyMapping.getInetPort());
                }
            }

            cache.put(tmpClient.getClientKey(), JSONArray.toJSONString(ports));
        }
        RedisUtils.delete(key);
        RedisUtils.hset(key, cache);
        return cache;
    }


    private static List<Integer> initUserPort(List<Client> clients){
        final String key = Constants.PRE_KEY + Constants.USER_PORT_REDIS_KEY;
        List<Integer>  cache = new ArrayList<>();
        for (Client tmpClient : clients) {
            if(tmpClient == null || tmpClient.getClientKey() == null){
                continue;
            }
            if(tmpClient.getProxyMappings() == null || tmpClient.getProxyMappings().isEmpty()){
                continue;
            }
            for (ClientProxyMapping proxyMapping : tmpClient.getProxyMappings()) {
                if(proxyMapping != null && proxyMapping.getInetPort() != null){
                     cache.add(proxyMapping.getInetPort());
                }
            }
        }
        RedisUtils.set(key, JSONArray.toJSONString(cache), Constants.CONFIG_DEFAULT_TIME);
        return cache;
    }
    private static Map<String, String> initLanInfo(List<Client> clients){
        final String key = Constants.PRE_KEY + Constants.LAN_REDIS_KEY;
        Map<String, String>  cache = new HashMap<>();
        for (Client tmpClient : clients) {
            if(tmpClient == null || tmpClient.getClientKey() == null){
                continue;
            }
            if(tmpClient.getProxyMappings() == null || tmpClient.getProxyMappings().isEmpty()){
                continue;
            }
            for (ClientProxyMapping proxyMapping : tmpClient.getProxyMappings()) {
                if(proxyMapping != null && proxyMapping.getInetPort() != null){
                    cache.put(String.valueOf(proxyMapping.getInetPort()), proxyMapping.getLan());
                }
            }
        }
        RedisUtils.set(key, JSONArray.toJSONString(cache), Constants.CONFIG_DEFAULT_TIME);
        return cache;
    }




}
