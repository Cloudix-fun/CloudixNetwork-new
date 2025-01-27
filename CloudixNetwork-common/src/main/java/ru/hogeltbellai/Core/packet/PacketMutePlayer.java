package ru.hogeltbellai.Core.packet;

import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.Core.Buf;
import ru.hogeltbellai.Core.PacketHandler;

public class PacketMutePlayer extends Packet {
    public String target;
    public String sender;
    public String reason;

    public PacketMutePlayer() {}

    public PacketMutePlayer(String target, String sender, String reason) {
        this.target = target;
        this.sender = sender;
        this.reason = reason;
    }

    @Override
    public void write0(Buf out) throws Exception {
        out.writeString(target);
        out.writeString(sender);
        out.writeString(reason);
    }

    @Override
    public void read0(Buf in) throws Exception {
        target = in.readString();
        sender = in.readString();
        reason = in.readString();
    }

    @Override
    protected void process0(IoSession session, PacketHandler handler) {
        handler.handlePacketMutePlayer(session,this);
    }
}
