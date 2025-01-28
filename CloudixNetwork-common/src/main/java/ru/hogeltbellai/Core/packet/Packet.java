package ru.hogeltbellai.Core.packet;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.Core.Buf;
import ru.hogeltbellai.Core.PacketHandler;

public abstract class Packet {
    public static final Map<Integer, PacketData> idToPacket = new ConcurrentHashMap<>();
    public static final Map<Class<? extends Packet>, Integer> classToId = new ConcurrentHashMap<>();

    static {
        registerPacket(1, PacketAnswer.class);
        registerPacket(2, PacketMessage.class);
        registerPacket(3, PacketPrivateMessage.class);
        registerPacket(4, PacketKickPlayer.class);
        registerPacket(5, PacketMutePlayer.class);
        registerPacket(6, PacketUpdater.class);
        registerPacket(7, PacketGetPlayerInfo.class);
        registerPacket(8, PacketPlayerInfo.class);
    }

    private final int id;

    public Packet() {
        this.id = classToId.getOrDefault(getClass(), -1);
        if (this.id == -1) {
            throw new IllegalStateException("Пакетный класс " + getClass().getName() + " не зарегистрирован");
        }
    }

    public void write(Buf buf) throws Exception {
        write0(buf);
    }

    public void read(Buf buf) throws Exception {
        read0(buf);
    }

    public void process(IoSession session, PacketHandler handler) throws Exception {
        handler.handle(session, this);
        process0(session, handler);
    }

    private static void registerPacket(int id, Class<? extends Packet> clazz) {
        idToPacket.put(id, new PacketData(clazz));
        classToId.put(clazz, id);
    }

    public static Packet createPacket(int id) throws ReflectiveOperationException {
        if (!idToPacket.containsKey(id)) {
            System.err.println("Не найден класс пакета для ID: " + id + ". Зарегистрированные ID: " + idToPacket.keySet());
            return null;
        }
        return idToPacket.get(id).create();
    }

    public static Class<? extends Packet> getPacketClass(int id) {
        if (!idToPacket.containsKey(id)) {
            System.err.println("Не найден класс пакета для ID: " + id + ". Зарегистрированные ID: " + idToPacket.keySet());
            return null;
        }
        return idToPacket.get(id).clazz;
    }

    public static int getPacketId(Class<? extends Packet> packetClass) {
        if (!classToId.containsKey(packetClass)) {
            System.err.println("Не найден ID для класса пакета: " + packetClass.getName());
            return -1;
        }
        return classToId.get(packetClass);
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public abstract void write0(Buf out) throws Exception;

    public abstract void read0(Buf in) throws Exception;

    protected abstract void process0(IoSession session, PacketHandler handler) throws Exception;

    public static class PacketData {
        private final Class<? extends Packet> clazz;
        private Constructor<? extends Packet> constructor;

        public PacketData(Class<? extends Packet> clazz) {
            this.clazz = clazz;
        }

        public Packet create() throws ReflectiveOperationException {
            if (constructor == null) {
                constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
            }
            return constructor.newInstance();
        }
    }
}
