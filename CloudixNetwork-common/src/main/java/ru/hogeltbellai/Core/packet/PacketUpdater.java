package ru.hogeltbellai.Core.packet;

import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.Core.Buf;
import ru.hogeltbellai.Core.PacketHandler;

public class PacketUpdater extends Packet {

    public PacketUpdater() {}

    @Override
    public void write0(Buf out) throws Exception {}

    @Override
    public void read0(Buf in) throws Exception {}

    @Override
    protected void process0(IoSession session, PacketHandler handler) throws Exception {
        handler.handlePacketUpdater(session,this);
    }
}
