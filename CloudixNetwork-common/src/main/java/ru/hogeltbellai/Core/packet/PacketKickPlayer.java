package ru.hogeltbellai.Core.packet;

import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.Core.Buf;
import ru.hogeltbellai.Core.PacketHandler;

public class PacketKickPlayer extends Packet {
    public String target;
    public String sender;
    public String reason;
    public KickType kickType;

    public PacketKickPlayer() {}

    public PacketKickPlayer(KickType kickType, String target, String sender, String reason) {
        this.kickType = kickType;
        this.target = target;
        this.sender = sender;
        this.reason = reason;
    }

    @Override
    public void write0(Buf out) throws Exception {
        out.writeString(target);
        out.writeString(sender);
        out.writeString(reason);
        out.writeString(kickType.name());
    }

    @Override
    public void read0(Buf in) throws Exception {
        target = in.readString();
        sender = in.readString();
        reason = in.readString();
        kickType = KickType.valueOf(in.readString());
    }

    @Override
    protected void process0(IoSession session, PacketHandler handler) {
        handler.handlePacketKickPlayer(session, this);
    }

    public enum KickType {
        KICK,
        BAN
    }
}

