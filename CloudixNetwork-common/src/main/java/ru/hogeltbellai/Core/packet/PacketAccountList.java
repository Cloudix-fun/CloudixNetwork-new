package ru.hogeltbellai.Core.packet;

import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.Core.Buf;
import ru.hogeltbellai.Core.PacketHandler;

import java.util.ArrayList;
import java.util.List;

public class PacketAccountList extends ResponsePacket {
    public String sender;
    public List<String> accountNames;

    public PacketAccountList() {
        this.accountNames = new ArrayList<>();
    }

    public PacketAccountList(String sender, List<String> accountNames) {
        this.sender = sender;
        this.accountNames = accountNames != null ? accountNames : new ArrayList<>();
    }

    @Override
    public void write0(Buf out) throws Exception {
        out.writeString(sender);
        out.writeInt(accountNames.size());

        for (String accountName : accountNames) {
            out.writeString(accountName);
        }
    }

    @Override
    public void read0(Buf in) throws Exception {
        sender = in.readString();
        int size = in.readInt();

        accountNames = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            accountNames.add(in.readString());
        }
    }

    @Override
    protected void process0(IoSession session, PacketHandler handler) {
        handler.handlePacketAccountList(session, this);
    }
}