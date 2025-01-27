package ru.hogeltbellai.Core.filter;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import ru.hogeltbellai.Core.Buf;
import ru.hogeltbellai.Core.packet.Packet;

public class PacketDecoder extends CumulativeProtocolDecoder {
    Packet lastSuccessPacket = null;

    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        int start = in.position();
        try {
            int index = 0;
            while (true) {
                byte get = in.get();
                if (get == Magic.BEGIN_BYTES[index]) {
                    index++;
                } else if (index != 0) {
                    if (get == Magic.BEGIN_BYTES[0]) {
                        index = 1;
                    } else {
                        index = 0;
                    }
                }
                if (index == Magic.BEGIN_LENGTH) {
                    int skipped = in.position() - start - Magic.BEGIN_LENGTH;
                    if (skipped > 0)
                        System.out.println("[Core Decoder] Skipped " + skipped + " bytes. Last packet: " + this.lastSuccessPacket);
                    int id = in.getShort();
                    Packet.PacketData data = Packet.idToPacket.get(id);
                    if (data == null) {
                        System.err.println("Wrong packet from " + session.getRemoteAddress() + ", packet id " + id);
                        return true;
                    }
                    Packet packet = data.create();
                    packet.read(new Buf(in));
                    out.write(packet);
                    this.lastSuccessPacket = packet;
                    return true;
                }
            }
        } catch (ProtocolDecoderException |java.nio.BufferUnderflowException ignored) {
            in.position(start);
            return false;
        }
    }
}
