package ru.hogeltbellai.Core.connector;

import lombok.Getter;
import ru.hogeltbellai.Core.packet.ResponsePacket;

@Getter
public class CallbackData {
    public final int requestId;
    public final Callback callback;
    public final long timeout;
    public final Runnable onTimeout;

    public CallbackData(int requestId, Callback callback, long timeout, Runnable onTimeout) {
        this.requestId = requestId;
        this.callback = callback;
        this.timeout = timeout;
        this.onTimeout = onTimeout;
    }

    public void executeCallback(ResponsePacket response) {
        callback.onResponse(response);
    }

    public void executeTimeout() {
        onTimeout.run();
    }
}
