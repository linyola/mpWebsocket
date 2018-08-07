package org.orial.websocket.link.entity;

import io.netty.channel.Channel;

/**
 */
public class UserInfo {
    private boolean isAuth = false; // 是否认证
    private long time = 0;  // 登录时间
    private long userId;     // UID
    private String token;    //
    private String addr;    // 地址
    private Channel channel;// 通道

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
