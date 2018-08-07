package org.orial.websocket.link.protocol;

/**
 */
public class ProtocolConstant {

    public static final int MESS_TYPE = 10001;
    public static final int AUTH_TYPE = 10002;
    public static final int PING_TYPE = 10003;
    public static final int PONG_TYPE = 10004;
    public static final int TIMEOUT_TYPE = 10005;
    public static final int CLOSE_TYPE = 10006;
    public static final int SYSTEM_TYPE = 10007;
    public static final int ERROR_TYPE = 10008;
    /**
     * 系统消息类型
     */
    public static final int SYS_USER_COUNT = 20001; // 在线用户数
    public static final int SYS_AUTH_STATE = 20002; // 认证结果
    public static final int SYS_OTHER_INFO = 20003; // 系统消息

}
