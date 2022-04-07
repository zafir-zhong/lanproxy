package org.fengfei.lanproxy.server.config;

import cn.hutool.core.util.StrUtil;
import org.fengfei.lanproxy.server.constant.Constants;

import java.util.Locale;

/**
 * @author zafir.zhong
 * @description
 * @date Created in 3:17 下午 2022/3/30.
 */
public class RedisConfig {

    private String type;
    private String servers;
    private String masterName;

    private String username;
    private String password;

    private int database;


    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if(type != null){
            type = type.toUpperCase(Locale.ROOT);
        }
        this.type = type;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public RedisConfig(String type, String servers, String masterName, String username, String password, Integer database) {
        if(type == null || type.isEmpty()){
            type = Constants.SINGLE;
        }else if(!type.equals(Constants.SENTINEL) && !type.equals(Constants.CLUSTER)){
            type = Constants.SINGLE;
        }
        this.type = type;
        this.servers = servers;
        this.masterName = masterName;
        if(StrUtil.isNotBlank(username)) {
            this.username = username;
        }
        this.password = password;
        if(database == null){
            database = 0;
        }
        this.database = database;
    }
}
