package ru.hogeltbellai.Core.filter;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import ru.hogeltbellai.Core.Buf;
import ru.hogeltbellai.Core.packet.Packet;

public class PacketEncoder implements ProtocolEncoder {

    @Override
    public void encode(IoSession session, Object obj, ProtocolEncoderOutput out) throws Exception {
        Packet packet = (Packet) obj;
        IoBuffer buf = IoBuffer.allocate(1024);
        buf.setAutoExpand(true);
        buf.put(Magic.BEGIN_BYTES);
        buf.putShort((short)packet.getId());
        packet.write(new Buf(buf));
        buf.flip();
        out.write(buf);
    }

    @Override
    public void dispose(IoSession session) throws Exception {}
}
