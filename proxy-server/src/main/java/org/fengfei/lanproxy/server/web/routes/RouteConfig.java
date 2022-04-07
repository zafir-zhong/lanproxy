package org.fengfei.lanproxy.server.web.routes;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import io.netty.util.internal.StringUtil;
import org.fengfei.lanproxy.common.JsonUtil;
import org.fengfei.lanproxy.server.ProxyChannelManager;
import org.fengfei.lanproxy.server.constant.Constants;
import org.fengfei.lanproxy.server.config.ProxyConfig;
import org.fengfei.lanproxy.server.entity.Client;
import org.fengfei.lanproxy.server.entity.UserInfo;
import org.fengfei.lanproxy.server.web.ApiRoute;
import org.fengfei.lanproxy.server.web.RequestHandler;
import org.fengfei.lanproxy.server.web.RequestMiddleware;
import org.fengfei.lanproxy.server.web.ResponseInfo;
import org.fengfei.lanproxy.server.web.exception.ContextException;
import org.fengfei.lanproxy.server.metrics.MetricsCollector;
import org.fengfei.lanproxy.server.utils.MysqlUtils;
import org.fengfei.lanproxy.server.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * 接口实现
 *
 * @author fengfei
 */
public class RouteConfig {

    protected static final String AUTH_COOKIE_KEY = "token";

    private static Logger logger = LoggerFactory.getLogger(RouteConfig.class);

    private static ThreadLocal<String> token = new ThreadLocal<>();

    /**
     * 管理员不能同时在多个地方登录
     */
    @Deprecated
//    private static String token;

    public static void init() {

        ApiRoute.addMiddleware(new RequestMiddleware() {

            @Override
            public void preRequest(FullHttpRequest request) {
                String cookieHeader = request.headers().get(HttpHeaders.Names.COOKIE);
                boolean authenticated = false;
                if (cookieHeader != null) {
                    String[] cookies = cookieHeader.split(";");
                    for (String cookie : cookies) {
                        String[] cookieArr = cookie.split("=");
                        if (AUTH_COOKIE_KEY.equals(cookieArr[0].trim())) {
                            // TODO 用户检查改为redis
                            if (cookieArr.length == 2 && checkToken(cookieArr[1])) {
                                authenticated = true;
                                token.set(cookieArr[1]);
                            }
                        }
                    }
                }

                String auth = request.headers().get(HttpHeaders.Names.AUTHORIZATION);
                if (!authenticated && auth != null) {
                    String[] authArr = auth.split(" ");
                    if (authArr.length == 2 && checkUser(authArr[0], authArr[1])) {
                        authenticated = true;
                    }
                }

                if (!request.getUri().equals("/login") && !authenticated) {
                    throw new ContextException(ResponseInfo.CODE_UNAUTHORIZED);
                }

                logger.info("handle request for api {}", request.getUri());
            }
        });

        // 获取配置详细信息
        ApiRoute.addRoute("/config/detail", new RequestHandler() {

            @Override
            public ResponseInfo request(FullHttpRequest request) {
                List<Client> clients = MysqlUtils.getClients();
                for (Client client : clients) {
                    Channel channel = ProxyChannelManager.getCmdChannel(client.getClientKey());
                    if (channel != null) {
                        client.setStatus(1);// online
                    } else {
                        client.setStatus(0);// offline
                    }
                }
                return ResponseInfo.build(clients);
            }
        });

        // 更新配置
        ApiRoute.addRoute("/config/update", new RequestHandler() {

            @Override
            public ResponseInfo request(FullHttpRequest request) {
                byte[] buf = new byte[request.content().readableBytes()];
                request.content().readBytes(buf);
                String config = new String(buf, Charset.forName("UTF-8"));
                List<Client> clients = JsonUtil.json2object(config, new TypeToken<List<Client>>() {
                });
                if (clients == null) {
                    return ResponseInfo.build(ResponseInfo.CODE_INVILID_PARAMS, "Error json config");
                }
                final List<Client> clientsInRunnable = clients;
                try {

                    MysqlUtils.updateClients(clientsInRunnable);

                } catch (Exception ex) {
                    logger.error("config update error", ex);
                    return ResponseInfo.build(ResponseInfo.CODE_INVILID_PARAMS, ex.getMessage());
                }

                return ResponseInfo.build(ResponseInfo.CODE_OK, "success");
            }
        });

        ApiRoute.addRoute("/login", new RequestHandler() {

            @Override
            public ResponseInfo request(FullHttpRequest request) {
                byte[] buf = new byte[request.content().readableBytes()];
                request.content().readBytes(buf);
                String config = new String(buf);
                Map<String, String> loginParams = JsonUtil.json2object(config, new TypeToken<Map<String, String>>() {
                });
                if (loginParams == null) {
                    return ResponseInfo.build(ResponseInfo.CODE_INVILID_PARAMS, "Error login info");
                }

                String username = loginParams.get("username");
                String password = loginParams.get("password");
                if (username == null || password == null) {
                    return ResponseInfo.build(ResponseInfo.CODE_INVILID_PARAMS, "Error username or password");
                }

                if (checkUser(username, password)) {
                    String token = UUID.randomUUID().toString().replace("-", "");
                    // TODO token应该缓存起来
                    setToken(token, MysqlUtils.getUserByName(username));
                    return ResponseInfo.build(token);
                }

                return ResponseInfo.build(ResponseInfo.CODE_INVILID_PARAMS, "Error username or password");
            }
        });

        ApiRoute.addRoute("/logout", new RequestHandler() {

            @Override
            public ResponseInfo request(FullHttpRequest request) {
                final String key = token.get();
                if (StrUtil.isNotBlank(key)) {
                    RedisUtils.deleteKey(key);
                }
                return ResponseInfo.build(ResponseInfo.CODE_OK, "success");
            }
        });

        ApiRoute.addRoute("/metrics/get", new RequestHandler() {

            @Override
            public ResponseInfo request(FullHttpRequest request) {
                return ResponseInfo.build(MetricsCollector.getAllMetrics());
            }
        });

        ApiRoute.addRoute("/metrics/getandreset", new RequestHandler() {

            @Override
            public ResponseInfo request(FullHttpRequest request) {
                return ResponseInfo.build(MetricsCollector.getAndResetAllMetrics());
            }
        });
    }


    public static boolean checkToken(String token) {
        final String key = RedisUtils.getKey(Constants.TOKEN + token);
        if (StringUtil.isNullOrEmpty(key)) {
            return false;
        }
        final UserInfo userInfo = JSONObject.parseObject(key, UserInfo.class);
        if (StringUtil.isNullOrEmpty(userInfo.getPermission())) {
            return false;
        }
        return userInfo.getPermission().contains(Constants.SERVICE);
    }

    public static void setToken(String token, UserInfo userInfo) {
        RedisUtils.setKey(Constants.TOKEN + token, JSONObject.toJSONString(userInfo), Constants.DEFAULT_TIME);
    }

    public static boolean checkUser(String username, String password) {
        if (StringUtil.isNullOrEmpty(password)) {
            return false;
        }
        final UserInfo userByName = MysqlUtils.getUserByName(username);
        if (userByName == null) {
            return false;
        }
        return SecureUtil.md5(password).equals(userByName.getPassword());
    }

    public static void main(String[] args) {
        System.out.println(SecureUtil.md5("Xuan1210"));
    }
}
