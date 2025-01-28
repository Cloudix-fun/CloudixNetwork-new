package ru.hogeltbellai.Core.packet;

import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.Core.Buf;
import ru.hogeltbellai.Core.PacketHandler;

public class PacketPlayerInfo extends ResponsePacket {
    public String playerName;
    public String serverName;

    public PacketPlayerInfo(String playerName, String serverName) {
        this.playerName = playerName;
        this.serverName = serverName;
    }

    public PacketPlayerInfo() { }

    @Override
    public void read0(Buf in) {
        playerName = in.readString();
        serverName = in.readString();
    }

    @Override
    public void write0(Buf out) {
        out.writeString(playerName);
        out.writeString(serverName != null ? serverName : "");
    }

    @Override
    protected void process0(IoSession session, PacketHandler handler) throws Exception {
        handler.handlePacketPlayerInfo(session, this);
    }

    @Override
    public String toString() {
        return "PacketPlayerInfo{playerName='" + playerName + "', serverName='" + serverName + "'}";
    }
}