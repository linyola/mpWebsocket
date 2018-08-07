package org.orial.websocket.link.protocol.fromClient;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.orial.websocket.link.entity.UserInfo;
import org.orial.websocket.link.handler.UserInfoService;

@Setter @Getter
@Slf4j
public abstract class Protocol implements Runnable{
    protected UserInfo userInfo;
    protected long userId;
    protected UserInfoService userInfoService;

    public abstract boolean process();
    @Override
    public final void run() {
        try {
            process();
        } catch (Exception e) {
            log.error("protocol execute Exception : {}", e);
        }
    }
}
