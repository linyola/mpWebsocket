package org.orial.websocket.link.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.orial.websocket.link.core.BaseServer;
import org.orial.websocket.link.handler.MessageHandler;
import org.orial.websocket.link.handler.UserAuthHandler;
import org.orial.websocket.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

@Slf4j
@Component
public class WebSocketServer extends BaseServer {

    @Autowired
    private UserAuthHandler userAuthHandler;
    @Autowired
    private MessageHandler messageHandler;
    @Autowired
    WebSocketService webSocketService;
    public WebSocketServer() {
        this.port = Constants.DEFAULT_PORT;
    }

    @PostConstruct
    @Override
    public void start() {
        init();
        log.info("starting web socket ... ");
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(defLoopGroup,
                                new HttpServerCodec(),   //请求解码器
                                new HttpObjectAggregator(65536),//将多个消息转换成单一的消息对象
                                new ChunkedWriteHandler(),  //支持异步发送大的码流，一般用于发送文件流
                                new IdleStateHandler(60, 0, 0), //检测链路是否读空闲
                                userAuthHandler, //处理握手和认证
                                messageHandler    //处理消息的发送
                        );
                    }
                });

        try {
            cf = b.bind().sync();
            InetSocketAddress addr = (InetSocketAddress) cf.channel().localAddress();
            log.info("WebSocketServer start success, port is:{}", addr.getPort());

        } catch (InterruptedException e) {
            log.error("WebSocketServer start fail,", e);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("AtWebSocketDestroy");
        shutdown();
        log.info("Finish WebSocketDestroy");
    }
}
