package ru.hogeltbellai.Core.packet;

import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.Core.Buf;
import ru.hogeltbellai.Core.PacketHandler;

public class PacketAnswer extends ResponsePacket {
    public String status;

    public PacketAnswer(String status) {
        this.status = status;
    }
    public PacketAnswer() { }

    @Override
    public void write0(Buf out) throws Exception {
        out.writeString(this.status);
    }

    @Override
    public void read0(Buf in) throws Exception {
        this.status = in.readString();
    }

    @Override
    protected void process0(IoSession session, PacketHandler handler) {
        handler.handleAnswer(session, this);
    }

    @Override
    public String toString() {
        return "PacketAnswer{status='" + status + "'}";
    }
}
