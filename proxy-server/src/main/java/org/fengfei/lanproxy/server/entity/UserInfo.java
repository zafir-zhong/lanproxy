package org.fengfei.lanproxy.server.entity;

/**
 * @author zafir.zhong
 * @description
 * @date Created in 4:48 下午 2022/4/2.
 */

public class UserInfo {

    private Long id;
    private String username;
    private String password;
    private String permission;
    private String role;
    private String phone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserInfo() {
    }

    public UserInfo(Long id, String username, String password, String permission, String role, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.permission = permission;
        this.role = role;
        this.phone = phone;
    }
}
