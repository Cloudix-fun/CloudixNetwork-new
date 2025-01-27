package ru.hogeltbellai.Core.connector;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import ru.hogeltbellai.CloudixNetwork.Debug;
import ru.hogeltbellai.Core.PacketHandler;
import ru.hogeltbellai.Core.filter.PacketDecoder;
import ru.hogeltbellai.Core.filter.PacketEncoder;
import ru.hogeltbellai.Core.packet.Packet;
import ru.hogeltbellai.Core.packet.ResponsePacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CoreNetwork {
    public static final Map<Long, IoSession> connectedClients = new ConcurrentHashMap<>();

    private IoAcceptor acceptor;
    private String host;
    private int port;

    private boolean reconnectEnabled = true;
    private boolean isEnabled;

    PacketHandler mainHandler;

    public void setMainHandler(PacketHandler handler) {
        this.mainHandler = handler;
    }

    public CoreNetwork(boolean isEnabled, String host, int port) {
        this.isEnabled = isEnabled;
        this.host = host;
        this.port = port;
    }

    public void connect() {
        if (!isEnabled) {
            return;
        }
        try {
            if (acceptor == null || acceptor.isDisposed()) {
                acceptor = new NioSocketAcceptor();
                acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new PacketEncoder(), new PacketDecoder()));
                acceptor.setHandler(new CoreHandler());
                if (this.mainHandler == null)
                    throw new IllegalStateException("Main handler не установлен");
                acceptor.bind(new InetSocketAddress(host, port));
                Debug.SOCKET.info("Mina сервер запущен на: " + host + ":" + port);
            } else {
                Debug.SOCKET.warning("Сервер уже запущен!");
            }
        } catch (IOException e) {
            Debug.SOCKET.logException("Ошибка при запуске Mina сервера", e);
            reconnect();
        }
    }

    public void disconnect() {
        if (!isEnabled) {
            return;
        }

        if (acceptor != null && !acceptor.isDisposed()) {
            acceptor.unbind();
            acceptor.dispose();
            Debug.SOCKET.info("Mina сервер остановлен");
        }

        for (IoSession session : connectedClients.values()) {
            session.closeNow();
        }
        connectedClients.clear();
        Debug.SOCKET.info("Все клиенты отключены");
    }

    public void reconnect() {
        if (!reconnectEnabled || !isEnabled) {
            return;
        }

        Debug.SOCKET.info("Попытка переподключения...");
        new Thread(() -> {
            while (reconnectEnabled && isEnabled) {
                try {
                    connect();
                    if (acceptor != null && !acceptor.isDisposed()) {
                        Debug.SOCKET.info("Переподключение успешно");
                        break;
                    }
                } catch (Exception e) {
                    Debug.SOCKET.logException("Ошибка при переподключении", e);
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isRunning() {
        return acceptor != null && !acceptor.isDisposed();
    }

    public static void broadcast(Packet packet) {
        if (connectedClients.isEmpty()) {
            return;
        }

        for (Map.Entry<Long, IoSession> entry : connectedClients.entrySet()) {
            IoSession clientSession = entry.getValue();
            if (clientSession != null && clientSession.isConnected()) {
                try {
                    clientSession.write(packet);
                    Debug.SOCKET.info("Пакет отправлен клиенту ID = " + entry.getKey());
                } catch (Exception e) {
                    Debug.SOCKET.warning("Ошибка при отправке пакета клиенту ID = " + entry.getKey() + ": " + e.getMessage());
                }
            }
        }
    }

    public static <T extends ResponsePacket> void sendPacketResponse(IoSession session, ResponsePacket originalPacket, T responsePacket) {
        originalPacket.setResponseId(responsePacket);
        session.write(responsePacket);
    }
}