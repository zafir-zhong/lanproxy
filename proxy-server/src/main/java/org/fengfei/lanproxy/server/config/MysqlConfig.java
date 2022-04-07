package org.fengfei.lanproxy.server.config;

/**
 * @author zafir.zhong
 * @description
 * @date Created in 11:19 上午 2022/4/6.
 */
public class MysqlConfig {

    private String url;
    private String username;
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MysqlConfig() {
    }

    public MysqlConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
}
