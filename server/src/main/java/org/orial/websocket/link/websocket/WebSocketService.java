package org.orial.websocket.link.websocket;

import lombok.extern.slf4j.Slf4j;
import org.orial.websocket.link.handler.UserInfoService;
import org.orial.websocket.link.protocol.fromClient.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WebSocketService {
    @Autowired
    private UserInfoService userInfoService;
    //用两个线程池，减少线程池的开销
    private ScheduledExecutorService executorTickService;
    private ScheduledExecutorService protocolThreadPool;
    public WebSocketService() {
        executorTickService = Executors.newScheduledThreadPool(1);
        protocolThreadPool = Executors.newScheduledThreadPool(4);
        // 定时扫描所有的Channel，关闭失效的Channel
        executorTickService.scheduleAtFixedRate(() ->{
            log.info("scanNotActiveChannel --------");
            userInfoService.scanNotActiveChannel();
        }, 3, 30000, TimeUnit.MILLISECONDS);
    }


    public void dispatch(final Protocol protocol) {
        protocolThreadPool.execute(protocol);
    }

    @PreDestroy
    public void shutdown() {
        if (executorTickService != null) {
            executorTickService.shutdown();
            protocolThreadPool.shutdown();
        }
    }
}
