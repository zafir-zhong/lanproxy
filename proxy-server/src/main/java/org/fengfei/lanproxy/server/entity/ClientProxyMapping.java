package org.fengfei.lanproxy.server.entity;

import java.util.List;

/**
 * @author zafir.zhong
 * @description
 * @date Created in 3:00 下午 2022/4/6.
 */
public class ClientProxyMapping {

    private Long id;
    private Long clientId;
    /**
     * 代理服务器端口
     */
    private Integer inetPort;

    /**
     * 需要代理的网络信息（代理客户端能够访问），格式 192.168.1.99:80 (必须带端口)
     */
    private String lan;

    /**
     * 备注名称
     */
    private String name;

    public Integer getInetPort() {
        return inetPort;
    }

    public void setInetPort(Integer inetPort) {
        this.inetPort = inetPort;
    }

    public String getLan() {
        return lan;
    }

    public void setLan(String lan) {
        this.lan = lan;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClientProxyMapping() {
    }

    public ClientProxyMapping(Long id, Long clientId, Integer inetPort, String lan, String name) {
        this.id = id;
        this.clientId = clientId;
        this.inetPort = inetPort;
        this.lan = lan;
        this.name = name;
    }

    public boolean checkMapping(){
        if(id == null || id.equals(0L)){
            return false;
        }
        return true;
    }
}
