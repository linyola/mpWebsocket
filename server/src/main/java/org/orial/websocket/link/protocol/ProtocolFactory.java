package org.orial.websocket.link.protocol;

import com.alibaba.fastjson.JSONObject;
import org.orial.websocket.link.entity.UserInfo;
import org.orial.websocket.link.handler.UserInfoService;
import org.orial.websocket.link.protocol.fromClient.Protocol;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProtocolFactory {

    @Resource
    private UserInfoService userInfoService;
    public Protocol newInstance(JSONObject jsonObject, UserInfo userInfo) {
        Protocol result = null;
        try {
            result = JSONObject.parseObject(jsonObject.getString("content"),
                    ((Class<Protocol>)Class.forName("org.orial.websocket.link.protocol.fromClient."+jsonObject.getString("type"))));
            result.setUserInfo(userInfo);
            result.setUserId(userInfo.getUserId());
            result.setUserInfoService(userInfoService);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }
}
