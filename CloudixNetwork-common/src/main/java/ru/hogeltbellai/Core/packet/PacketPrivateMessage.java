package ru.hogeltbellai.Core.packet;

import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.Core.Buf;
import ru.hogeltbellai.Core.PacketHandler;

public class PacketPrivateMessage extends ResponsePacket {
    public String sender;
    public String target;
    public String message;

    public PacketPrivateMessage(String sender, String target, String message) {
        this.sender = sender;
        this.target = target;
        this.message = message;
    }
    public PacketPrivateMessage() { }

    @Override
    public void read0(Buf in) {
        sender = in.readString();
        target = in.readString();
        message = in.readString();
    }

    @Override
    public void write0(Buf out) {
        out.writeString(sender);
        out.writeString(target);
        out.writeString(message);
    }

    @Override
    protected void process0(IoSession session, PacketHandler handler) {
        handler.handlePacketPrivateMessage(session, this);
    }

    @Override
    public String toString() {
        return "PacketPrivateMessage{sender='" + sender + "', target='" + target + "', message='" + message + "'}";
    }
}