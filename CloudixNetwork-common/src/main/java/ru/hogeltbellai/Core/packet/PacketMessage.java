package ru.hogeltbellai.Core.packet;

import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.Core.Buf;
import ru.hogeltbellai.Core.PacketHandler;

public class PacketMessage extends Packet {
    public String message;

    public PacketMessage() {}

    public PacketMessage(String message) {
        this.message = message;
    }

    @Override
    public void write0(Buf out) throws Exception {
        out.writeString(message);
    }

    @Override
    public void read0(Buf in) throws Exception {
        message = in.readString();
    }

    @Override
    protected void process0(IoSession session, PacketHandler handler) {
        handler.handlePacketMessage(session,this);
    }
}
