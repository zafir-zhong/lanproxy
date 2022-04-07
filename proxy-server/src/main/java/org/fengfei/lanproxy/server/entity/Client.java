package org.fengfei.lanproxy.server.entity;



import java.util.List;

/**
 * @author zafir.zhong
 * @description
 * @date Created in 2:58 下午 2022/4/6.
 */
public class Client {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 客户端备注名称 */
    private String name;

    /** 代理客户端唯一标识key */
    private String clientKey;

    /** 代理客户端与其后面的真实服务器映射关系 */
    private List<ClientProxyMapping> proxyMappings;

    private int status;

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public List<ClientProxyMapping> getProxyMappings() {
        return proxyMappings;
    }

    public void setProxyMappings(List<ClientProxyMapping> proxyMappings) {
        this.proxyMappings = proxyMappings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Client() {
    }

    public Client(Long id, String name, String clientKey, int status) {
        this.id = id;
        this.name = name;
        this.clientKey = clientKey;
        this.status = status;
    }
}
