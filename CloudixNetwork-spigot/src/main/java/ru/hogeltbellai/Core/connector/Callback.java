package ru.hogeltbellai.Core.connector;

import ru.hogeltbellai.Core.packet.ResponsePacket;

public interface Callback {
    void onResponse(ResponsePacket response);
}
