package ru.hogeltbellai.Core.connector;

import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.bukkit.Bukkit;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.Debug;
import ru.hogeltbellai.Core.PacketHandler;
import ru.hogeltbellai.Core.filter.PacketDecoder;
import ru.hogeltbellai.Core.filter.PacketEncoder;
import ru.hogeltbellai.Core.packet.Packet;
import ru.hogeltbellai.Core.packet.ResponsePacket;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CoreConnector {
    public final ConcurrentHashMap<Integer, CallbackData> callbacks = new ConcurrentHashMap<>();
    private final AtomicInteger callbackCounter = new AtomicInteger(0);

    private IoConnector connector;
    private IoSession session;
    private final String host;
    private final int port;
    private final boolean reconnectEnabled = true;
    private final boolean isEnabled;

    PacketHandler mainHandler;

    public void setMainHandler(PacketHandler handler) {
        this.mainHandler = handler;
    }

    public CoreConnector(boolean isEnabled, String host, int port) {
        this.isEnabled = isEnabled;
        this.host = host;
        this.port = port;
    }

    public void connect() {
        if (!isEnabled)
            return;

        try {
            if (connector == null || !connector.isActive() || session == null || !session.isConnected()) {
                connector = new NioSocketConnector();
                connector.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new PacketEncoder(), new PacketDecoder()));
                connector.setHandler(new CoreHandler());
                if (this.mainHandler == null)
                    throw new IllegalStateException("Main handler не установлен");
                connector.connect(new InetSocketAddress(host, port)).addListener(future -> {
                    if (future.isDone()) {
                        session = future.getSession();
                        if (session != null && session.isConnected()) {
                            Debug.SOCKET.info("Подключение к серверу ядру");
                        } else {
                            Debug.SOCKET.warning("Не удалось подключиться к ядру");
                            reconnect();
                        }
                    }
                });
            } else {
                Debug.SOCKET.warning("Подключение уже установлено!");
            }
        } catch (Exception e) {
            Debug.SOCKET.logException("Ошибка при подключении к ядру", e);
            reconnect();
        }
    }

    public void disconnect() {
        if (!isEnabled)
            return;

        if (session != null && session.isConnected()) {
            session.closeNow();
            Debug.SOCKET.info("Отключение от ядра");
        }
    }

    public void reconnect() {
        if (!reconnectEnabled || !isEnabled)
            return;

        Debug.SOCKET.info("Попытка переподключения к ядру...");
        new Thread(() -> {
            while (reconnectEnabled && isEnabled) {
                try {
                    connect();
                    if (session != null && session.isConnected()) {
                        Debug.SOCKET.info("Переподключение к ядру успешно");
                        break;
                    }
                } catch (Exception e) {
                    Debug.SOCKET.logException("Ошибка при переподключении к ядру", e);
                }

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    public void sendPacket(Packet packet) {
        if (isConnected()) {
            try {
                this.session.write(packet);
                Debug.SOCKET.info("Отправлен пакет " + packet.getId());
            } catch (Exception e) {
                Debug.SOCKET.logException("Ошибка при отправке пакета", e);
            }
        }
    }

    public void sendRequest(ResponsePacket packet, Callback callback, long timeout) {
        int requestId = callbackCounter.getAndIncrement();
        CallbackData callbackData = new CallbackData(
                requestId,
                callback,
                timeout,
                () -> {
                    System.err.println("Запрос с ID " + requestId + " истёк по времени.");
                }
        );

        callbacks.put(requestId, callbackData);
        packet.pResponseId = requestId;

        sendPacket(packet);

        Bukkit.getScheduler().runTaskLater(CNPluginSpigot.core(), () -> {
            CallbackData data = callbacks.remove(requestId);
            if (data != null) {
                data.executeTimeout();
            }
        }, timeout / 50L);
    }
}