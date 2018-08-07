package org.orial.websocket.link.protocol;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class ProtocolType {

    private int version = 1;
    private int code;
    private String body;
    private Map<String,Object> extend = new HashMap<>();

    public ProtocolType(int type, String body) {
        this.code = type;
        this.body = body;
    }

    public static String buildPingProto() {
        return buildProto(ProtocolConstant.PING_TYPE, null);
    }

    public static String buildPongProto() {
        return buildProto(ProtocolConstant.PONG_TYPE, null);
    }

    public static String buildSystProto(int type, Object mess) {
        ProtocolType chatProto = new ProtocolType(ProtocolConstant.SYSTEM_TYPE, null);
        chatProto.extend.put("type", type);
        chatProto.extend.put("mess", mess);
        return JSONObject.toJSONString(chatProto);
    }

    public static String buildAuthProto(boolean isSuccess) {
        ProtocolType chatProto = new ProtocolType(ProtocolConstant.AUTH_TYPE, null);
        chatProto.extend.put("isSuccess", isSuccess);
        return JSONObject.toJSONString(chatProto);
    }

    public static String buildErorProto(int type, String mess) {
        ProtocolType chatProto = new ProtocolType(ProtocolConstant.ERROR_TYPE, null);
        chatProto.extend.put("type", type);
        chatProto.extend.put("mess", mess);
        return JSONObject.toJSONString(chatProto);
    }

    public static String buildMessProto(String protocolName, String mess) {
        ProtocolType chatProto = new ProtocolType(ProtocolConstant.MESS_TYPE, mess);
        chatProto.extend.put("type", protocolName);
        return JSONObject.toJSONString(chatProto);
    }

    public static String buildProto(int type, String body) {
        ProtocolType chatProto = new ProtocolType(type, body);
        return JSONObject.toJSONString(chatProto);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Map<String, Object> getExtend() {
        return extend;
    }

    public void setExtend(Map<String, Object> extend) {
        this.extend = extend;
    }
}
