package org.fengfei.lanproxy.server.utils;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.fengfei.lanproxy.server.config.ProxyConfig;
import org.fengfei.lanproxy.server.config.RedisConfig;
import redis.clients.jedis.*;
import redis.clients.jedis.util.Pool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.fengfei.lanproxy.server.constant.Constants.CHANNEL;

/**
 * @author zafir.zhong
 * @description
 * @date Created in 3:14 下午 2022/3/30.
 */
public class RedisUtils {

    private static Pool<Jedis> jedisPool;
    private static JedisCluster jedisCluster;
    private static int type;


    private static RedisConfig redisConfig;

    static {
        redisConfig = ProxyConfig.getInstance().getRedisConfig();
        switch (redisConfig.getType()) {
            case "CLUSTER":
                type = 1;
                Set<HostAndPort> nodes = new HashSet<>();
                for (String url : redisConfig.getServers().split(",")) {
                    final String[] split = url.split(":");
                    if (split.length == 2) {
                        nodes.add(new HostAndPort(split[0], Integer.valueOf(split[1])));
                    }
                }
                jedisCluster = new JedisCluster(nodes, redisConfig.getUsername(), redisConfig.getPassword());
                break;
            case "SENTINEL":
                type = 2;
                Set<String> servers = new HashSet<>(Arrays.asList(redisConfig.getServers().split(",")));
                jedisPool = new JedisSentinelPool(redisConfig.getMasterName(), servers,
                        new GenericObjectPoolConfig<Jedis>(),
                        Protocol.DEFAULT_TIMEOUT,
                        redisConfig.getUsername(), redisConfig.getPassword(), redisConfig.getDatabase());
                break;
            case "SINGLE":
                type = 3;
                final String[] split = redisConfig.getServers().split(":");
                int port = 6379;
                if (split.length == 2) {
                    port = Integer.valueOf(split[1]);
                }
                jedisPool = new JedisPool(new GenericObjectPoolConfig<Jedis>(), split[0], port,
                        Protocol.DEFAULT_TIMEOUT, redisConfig.getUsername(), redisConfig.getPassword(), redisConfig.getDatabase());
                break;
            default:
        }
        // 订阅
        if (type > 1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    jedisPool.getResource().subscribe(
                            new JedisPubSub() {
                                @Override
                                public void onMessage(String channel, String message) {
                                    super.onMessage(channel, message);
                                    // TODO 触发改动相关操作
                                }
                            },
                            CHANNEL);
                }
            }).start();
        }else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    jedisCluster.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            super.onMessage(channel, message);
                            // TODO 触发改动相关操作
                        }
                    }, CHANNEL);
                }
            }).start();
        }
    }

    public static String setKey(String key, String value, int time) {
        if (type > 1) {
            final Jedis resource = jedisPool.getResource();
            final String setex = resource.setex(key, time, value);
            resource.close();
            return setex;
        }
        return jedisCluster.setex(key, time, value);
    }


    public static String getKey(String key) {
        if (type > 1) {
            final Jedis resource = jedisPool.getResource();
            final String s = resource.get(key);
            resource.close();
            return s;
        }
        return jedisCluster.get(key);
    }

    public static long deleteKey(String key) {
        if (type > 1) {
            final Jedis resource = jedisPool.getResource();
            final long del = resource.del(key);
            resource.close();

            return del;
        }
        return jedisCluster.del(key);
    }

    public static long publish(String msg) {
        if (type > 1) {
            final Jedis resource = jedisPool.getResource();
            final long publish = resource.publish(CHANNEL, msg);
            resource.close();
            return publish;
        }
        return jedisCluster.publish(CHANNEL, msg);
    }


}
