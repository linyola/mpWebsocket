package org.orial.websocket.link.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.orial.websocket.link.entity.UserInfo;
import org.orial.websocket.link.protocol.ProtocolType;
import org.orial.websocket.link.protocol.toClient.ToProtocol;
import org.orial.websocket.utils.NettyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Channel的管理器
 */
@Slf4j
@Service
public class UserInfoService {

    @Autowired
    private RedisOperationsSessionRepository redisOperationsSessionRepository;
    private static Map<Channel, UserInfo> channel2Users = new ConcurrentHashMap<>();
    private static Map<Long, Channel> userId2Channel = new ConcurrentHashMap<>();
    private static AtomicInteger userCount = new AtomicInteger(0);
    public void addChannel(Channel channel) {
        String remoteAddr = NettyUtil.parseChannelRemoteAddr(channel);
        if (!channel.isActive()) {
            log.error("channel is not active, address: {}", remoteAddr);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setAddr(remoteAddr);
        userInfo.setChannel(channel);
        userInfo.setTime(System.currentTimeMillis());

        log.warn("add channel, address is :{}", NettyUtil.parseChannelRemoteAddr(channel));
        channel2Users.put(channel, userInfo);
    }

    public boolean saveUser(Channel channel, String token, long userId) {
        UserInfo userInfo = channel2Users.get(channel);
        if (userInfo == null) {
            return false;
        }
        if (!channel.isActive()) {
            log.error("channel is not active, address: {}, nick: {}", userInfo.getAddr(), token);
            return false;
        }
        String sessionId = token.split("=")[1].split(";")[0];
        Session session = redisOperationsSessionRepository.getSession(sessionId);
        if(session==null || (long)session.getAttribute("userId") != userId) {
            log.error("无法建立长连接，因为信息不匹配！userId:{}", userId);
            channel.close();
            return false;
        }
        userId2Channel.put(userId, channel);
        // 增加一个认证用户
        userCount.incrementAndGet();
        userInfo.setToken(token);
        userInfo.setAuth(true);
        userInfo.setUserId(userId);
        userInfo.setTime(System.currentTimeMillis());
        log.warn("add user, nick:{}", token);

        return true;
    }

    /**
     * 从缓存中移除Channel，并且关闭Channel
     *
     * @param channel
     */
    public void removeChannel(Channel channel) {
        if(!channel2Users.containsKey(channel)) {
            return ;
        }
        log.warn("channel will be remove, address is :{}", NettyUtil.parseChannelRemoteAddr(channel));
        channel.close();
        UserInfo userInfo = channel2Users.get(channel);
        if (userInfo != null) {
            UserInfo tmp = channel2Users.remove(channel);
            if (tmp != null && tmp.isAuth()) {
                // 减去一个认证用户
                userCount.decrementAndGet();

                userId2Channel.remove(tmp.getUserId());
            }
        }

    }

    /**
     * 发送系统消息
     *
     * @param code
     * @param mess
     */
    public void sendInfo(Channel channel, int code, Object mess) {
        channel.writeAndFlush(new TextWebSocketFrame(ProtocolType.buildSystProto(code, mess)));
    }

    /**
     * 发送协议消息
     *
     * @param protocolName
     * @param mess
     */
    public void sendMessage(long userId, String protocolName, String mess) {
        Channel channel = getChannelByUserId(userId);
        if(channel == null) {
            log.info("send message error, userId : {}, message : {}", userId, mess);
            return ;
        }
        channel.writeAndFlush(new TextWebSocketFrame(ProtocolType.buildMessProto(protocolName, mess)));
    }
    /**
     * 发送协议消息
     *
     * @param userId    玩家id
     * @param protocol  协议
     */
    public void sendMessage(long userId, ToProtocol protocol) {
        Channel channel = getChannelByUserId(userId);
        if(channel == null) {
            log.debug("send message error2, userId : {}, message : {}", userId, JSON.toJSONString(protocol));
            return ;
        }
        String mess = JSON.toJSONString(protocol);
        channel.writeAndFlush(new TextWebSocketFrame(ProtocolType.buildMessProto(protocol.getName(), mess)));
    }

    /**
     *
     * @param userId
     * @return
     */
    private Channel getChannelByUserId(long userId) {
        return userId2Channel.get(userId);
    }

    /**
     * 广播系统消息
     */
    public void broadcastInfo(int code, Object mess) {
        Set<Channel> keySet = channel2Users.keySet();
        for (Channel ch : keySet) {
            UserInfo userInfo = channel2Users.get(ch);
            if (userInfo == null || !userInfo.isAuth()) continue;
            ch.writeAndFlush(new TextWebSocketFrame(ProtocolType.buildSystProto(code, mess)));
        }
    }

    /**
     * 广播协议消息
     */
    public void broadcastMessage(ToProtocol protocol) {
        String mess = JSON.toJSONString(protocol);
        Set<Channel> keySet = channel2Users.keySet();
        for (Channel ch : keySet) {
            UserInfo userInfo = channel2Users.get(ch);
            if (userInfo == null || !userInfo.isAuth()) continue;
            ch.writeAndFlush(new TextWebSocketFrame(ProtocolType.buildMessProto(protocol.getName(), mess)));
        }
    }

    /**
     * 给指定用户群体发送协议消息
     */
    public void sendMessage(List<Long> users, ToProtocol protocol) {
        if(users == null || users.isEmpty()) {
            return ;
        }
        String mess = JSON.toJSONString(protocol);
        for (Long userId : users) {
            Channel ch = userId2Channel.get(userId);
            if(ch == null) {
                continue;
            }
            UserInfo userInfo = channel2Users.get(ch);
            if (userInfo == null || !userInfo.isAuth())
                continue;
            ch.writeAndFlush(new TextWebSocketFrame(ProtocolType.buildMessProto(protocol.getName(), mess)));
        }
    }

    public void sendPong(Channel channel) {
        channel.writeAndFlush(new TextWebSocketFrame(ProtocolType.buildPongProto()));
    }

    /**
     * 扫描并关闭失效的Channel
     */
    public void scanNotActiveChannel() {
        Set<Channel> keySet = channel2Users.keySet();
        for (Channel ch : keySet) {
            UserInfo userInfo = channel2Users.get(ch);
            if (userInfo == null) continue;
            if (!ch.isOpen() || !ch.isActive() || (!userInfo.isAuth() &&
                    (System.currentTimeMillis() - userInfo.getTime()) > 10000)) {
                removeChannel(ch);
            }
        }
    }


    public UserInfo getUserInfo(Channel channel) {
        return channel2Users.get(channel);
    }

    public int getAuthUserCount() {
        return userCount.get();
    }

    public void updateUserTime(Channel channel) {
        UserInfo userInfo = getUserInfo(channel);
        if (userInfo != null) {
            userInfo.setTime(System.currentTimeMillis());
        }
    }

    /**
     * 判断是否长连接在线
     * @return
     */
    public boolean isOnline(long userId) {
        return userId2Channel.get(userId) != null;
    }
}
