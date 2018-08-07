package org.orial.websocket.link.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.orial.websocket.link.entity.UserInfo;
import org.orial.websocket.link.protocol.ProtocolConstant;
import org.orial.websocket.utils.Constants;
import org.orial.websocket.utils.NettyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 */
@Slf4j
@Service
@ChannelHandler.Sharable
public class UserAuthHandler extends SimpleChannelInboundHandler<Object> {

    @Autowired
    private UserInfoService userInfoService;
    private WebSocketServerHandshaker handshaker;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocket(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent evnet = (IdleStateEvent) evt;
            // 判断Channel是否读空闲, 读空闲时移除Channel
            if (evnet.state().equals(IdleState.READER_IDLE)) {
                final String remoteAddress = NettyUtil.parseChannelRemoteAddr(ctx.channel());
                log.warn("NETTY SERVER PIPELINE: IDLE exception [{}]", remoteAddress);
                userInfoService.removeChannel(ctx.channel());
                userInfoService.broadcastInfo(ProtocolConstant.SYS_USER_COUNT, userInfoService.getAuthUserCount());
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!request.decoderResult().isSuccess() || !"websocket".equals(request.headers().get("Upgrade"))) {
            log.warn("protobuf don't support websocket");
            ctx.channel().close();
            return;
        }
        WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(
                Constants.WEBSOCKET_URL, null, true);
        handshaker = handshakerFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            // 动态加入websocket的编解码处理
            handshaker.handshake(ctx.channel(), request);
            UserInfo userInfo = new UserInfo();
            userInfo.setAddr(NettyUtil.parseChannelRemoteAddr(ctx.channel()));
            // 存储已经连接的Channel
            userInfoService.addChannel(ctx.channel());
        }
    }

    private void handleWebSocket(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否关闭链路命令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            userInfoService.removeChannel(ctx.channel());
            return;
        }
        // 判断是否Ping消息
        if (frame instanceof PingWebSocketFrame) {
//            log.info("ping message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 判断是否Pong消息
        if (frame instanceof PongWebSocketFrame) {
//            log.info("pong message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        // 本程序目前只支持文本消息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(frame.getClass().getName() + " frame type not supported");
        }
        String message = ((TextWebSocketFrame) frame).text();
        JSONObject json = JSONObject.parseObject(message);
        int type = json.getInteger("type");
        Channel channel = ctx.channel();
        switch (type) {
            case ProtocolConstant.PING_TYPE:
            case ProtocolConstant.PONG_TYPE:
                userInfoService.updateUserTime(channel);
                userInfoService.sendPong(ctx.channel());
//                log.info("receive pong message, address: {}",NettyUtil.parseChannelRemoteAddr(channel));
                return;
            case ProtocolConstant.AUTH_TYPE:
                String token = json.getJSONObject("content").getString("token");
                long userId = json.getJSONObject("content").getLong("userId");
                if(StringUtils.isBlank(token)) {
                    channel.close();

                    return;
                }
                boolean isSuccess = userInfoService.saveUser(channel, token, userId);
                userInfoService.sendInfo(channel, ProtocolConstant.SYS_AUTH_STATE,isSuccess);
                if (isSuccess) {
                    userInfoService.broadcastInfo(ProtocolConstant.SYS_USER_COUNT, userInfoService.getAuthUserCount());
                }
                return;
            case ProtocolConstant.MESS_TYPE: //普通的消息留给MessageHandler处理
                break;
            default:
                log.warn("The code [{}] can't be auth!!!", type);
                return;
        }
        //后续消息交给MessageHandler处理
        ctx.fireChannelRead(frame.retain());
    }
}
