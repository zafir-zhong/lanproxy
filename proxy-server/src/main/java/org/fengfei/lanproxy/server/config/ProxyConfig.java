package org.fengfei.lanproxy.server.config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fengfei.lanproxy.common.Config;
import org.fengfei.lanproxy.common.JsonUtil;
import org.fengfei.lanproxy.server.entity.Client;
import org.fengfei.lanproxy.server.entity.ClientProxyMapping;
import org.fengfei.lanproxy.server.utils.ProxyUtils;
import org.fengfei.lanproxy.server.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

/**
 * server config
 *
 * @author fengfei
 *
 */
public class ProxyConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 配置文件为config.json */
    public static final String CONFIG_FILE;

    private static Logger logger = LoggerFactory.getLogger(ProxyConfig.class);

    static {

        // 代理配置信息存放在用户根目录下
        String property = System.getProperty("lanproxy.home");
        if(property==null || property.length() == 0) {
            property = System.getProperty("user.home");
        }
        String dataPath = property + "/" + ".lanproxy/";
        File file = new File(dataPath);
        if (!file.isDirectory()) {
            file.mkdir();
        }
        CONFIG_FILE = dataPath + "/config.json";
    }

    /** 代理服务器绑定主机host */
    private String serverBind;

    /** 代理服务器与代理客户端通信端口 */
    private Integer serverPort;

    /** 配置服务绑定主机host */
    private String configServerBind;

    /** 配置服务端口 */
    private Integer configServerPort;

    /** 配置服务管理员用户名 */
    @Deprecated
    private String configAdminUsername;

    /** 配置服务管理员密码 */
    @Deprecated
    private String configAdminPassword;

    private RedisConfig redisConfig;

    private MysqlConfig mysqlConfig;

    /** 更新配置后保证在其他线程即时生效 */
    private static ProxyConfig instance = new ProxyConfig();

    private ProxyConfig() {

        // 代理服务器主机和端口配置初始化
        this.serverPort = Config.getInstance().getIntValue("server.port");
        this.serverBind = Config.getInstance().getStringValue("server.bind", "0.0.0.0");

        // 配置服务器主机和端口配置初始化
        this.configServerPort = Config.getInstance().getIntValue("config.server.port");
        this.configServerBind = Config.getInstance().getStringValue("config.server.bind", "0.0.0.0");

        // 配置服务器管理员登录认证信息
        this.configAdminUsername = Config.getInstance().getStringValue("config.admin.username");
        this.configAdminPassword = Config.getInstance().getStringValue("config.admin.password");

        this.mysqlConfig = new MysqlConfig(
                Config.getInstance().getStringValue("config.mysql.url"),
                Config.getInstance().getStringValue("config.mysql.username"),
                Config.getInstance().getStringValue("config.mysql.password")
        );
        this.redisConfig = new RedisConfig(
                Config.getInstance().getStringValue("config.redis.type"),
                Config.getInstance().getStringValue("config.redis.servers"),
                Config.getInstance().getStringValue("config.redis.master"),
                Config.getInstance().getStringValue("config.redis.username"),
                Config.getInstance().getStringValue("config.redis.password"),
                Config.getInstance().getIntValue("config.redis.database")
                );
        logger.info(
                "config init serverBind {}, serverPort {}, configServerBind {}, configServerPort {}, configAdminUsername {}, configAdminPassword {}",
                serverBind, serverPort, configServerBind, configServerPort, configAdminUsername, configAdminPassword);

        ProxyUtils.notifyconfigChangedListeners(false);
    }

    public Integer getServerPort() {
        return this.serverPort;
    }

    public String getServerBind() {
        return serverBind;
    }

    public void setServerBind(String serverBind) {
        this.serverBind = serverBind;
    }

    public String getConfigServerBind() {
        return configServerBind;
    }

    public void setConfigServerBind(String configServerBind) {
        this.configServerBind = configServerBind;
    }

    public Integer getConfigServerPort() {
        return configServerPort;
    }

    public void setConfigServerPort(Integer configServerPort) {
        this.configServerPort = configServerPort;
    }

    public String getConfigAdminUsername() {
        return configAdminUsername;
    }

    public void setConfigAdminUsername(String configAdminUsername) {
        this.configAdminUsername = configAdminUsername;
    }

    public String getConfigAdminPassword() {
        return configAdminPassword;
    }

    public void setConfigAdminPassword(String configAdminPassword) {
        this.configAdminPassword = configAdminPassword;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }



    public RedisConfig getRedisConfig() {
        return redisConfig;
    }

    public void setRedisConfig(RedisConfig redisConfig) {
        this.redisConfig = redisConfig;
    }

    public MysqlConfig getMysqlConfig() {
        return mysqlConfig;
    }

    public void setMysqlConfig(MysqlConfig mysqlConfig) {
        this.mysqlConfig = mysqlConfig;
    }





    public static ProxyConfig getInstance() {
        return instance;
    }

    /**
     * 配置更新回调
     *
     * @author fengfei
     *
     */
    public static interface ConfigChangedListener {

        void onChanged();
    }
}
