package org.orial.websocket.link.protocol.toClient;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class ToProtocol {
    protected String name;
    public ToProtocol() {
        this.name = this.getClass().getSimpleName();
    }
}
