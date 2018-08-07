package org.orial.websocket.link.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.orial.websocket.link.entity.UserInfo;
import org.orial.websocket.link.protocol.ProtocolConstant;
import org.orial.websocket.link.protocol.ProtocolFactory;
import org.orial.websocket.link.websocket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 */
@Slf4j
@Service
@ChannelHandler.Sharable
public class MessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Resource
    private ProtocolFactory protocolFactory;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    WebSocketService webSocketService;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        try {
            UserInfo userInfo = userInfoService.getUserInfo(ctx.channel());
            if (userInfo != null && userInfo.isAuth()) {
                webSocketService.dispatch(protocolFactory.newInstance(JSONObject.parseObject(frame.text()).getJSONObject("content"), userInfo));
            }
        } catch (Exception e) {
            log.error("message handler error:{}", e);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        userInfoService.removeChannel(ctx.channel());
        userInfoService.broadcastInfo(ProtocolConstant.SYS_USER_COUNT, userInfoService.getAuthUserCount());
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("connection error and close the channel", cause);
        userInfoService.removeChannel(ctx.channel());
        userInfoService.broadcastInfo(ProtocolConstant.SYS_USER_COUNT, userInfoService.getAuthUserCount());
    }

}
