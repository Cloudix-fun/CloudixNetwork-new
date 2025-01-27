package ru.hogeltbellai.Core.connector;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.Debug;
import ru.hogeltbellai.Core.packet.Packet;
import ru.hogeltbellai.Core.packet.ResponsePacket;

public class CoreHandler extends IoHandlerAdapter {

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        Debug.SOCKET.info("Соединение с ядром установлено: " + session.getRemoteAddress());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        Debug.SOCKET.info("Соединение с ядром закрыто: " + session.getRemoteAddress());
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        Packet packet = (Packet) message;
        try {
            packet.process(session, CNPluginSpigot.core().getCoreConnector().mainHandler);
        } catch (Exception ex) {
            Debug.SOCKET.logException("Packet: " + packet, ex);
        }
        if (packet instanceof ResponsePacket) {
            int requestId = ((ResponsePacket) packet).pResponseId;

            CallbackData data = CNPluginSpigot.core().getCoreConnector().callbacks.remove(requestId);
            if (data != null) {
                data.callback.onResponse((ResponsePacket) packet);
            }
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        Debug.SOCKET.warning("Ошибка на соединении ID " + session.getId() + ": " + cause.getMessage());

        if (cause instanceof java.net.SocketException) {
            Debug.SOCKET.info("Соединение сброшено. Переподключение...");
            session.closeNow();
            if(!CNPluginSpigot.core().getCoreConnector().isConnected())
                CNPluginSpigot.core().getCoreConnector().reconnect();
        } else {
            session.closeNow();
            if(!CNPluginSpigot.core().getCoreConnector().isConnected())
                CNPluginSpigot.core().getCoreConnector().reconnect();
        }
    }
}
