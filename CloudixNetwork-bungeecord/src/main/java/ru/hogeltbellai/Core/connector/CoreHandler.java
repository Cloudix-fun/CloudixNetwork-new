package ru.hogeltbellai.Core.connector;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import ru.hogeltbellai.CloudixNetwork.CNPluginBungee;
import ru.hogeltbellai.CloudixNetwork.Debug;
import ru.hogeltbellai.Core.packet.Packet;

public class CoreHandler extends IoHandlerAdapter {

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        CoreNetwork.connectedClients.put(session.getId(), session);
        Debug.SOCKET.info("Новое соединение: ID = " + session.getId());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        CoreNetwork.connectedClients.remove(session.getId());
        Debug.SOCKET.info("Соединение закрыто: ID = " + session.getId());
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        Debug.SOCKET.info("Сообщение от клиента (ID " + session.getId() + "): " + message);
        Packet packet = (Packet) message;

        try {
            packet.process(session, CNPluginBungee.core().getCoreNetwork().mainHandler);
        } catch (Exception ex) {
            Debug.SOCKET.warning("Packet: " + packet);
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        Debug.SOCKET.warning("Ошибка на соединении ID " + session.getId() + ": " + cause.getMessage());

        if (cause instanceof java.net.SocketException) {
            Debug.SOCKET.info("Соединение сброшено (обычное отключение клиента). Попытка закрытия сессии...");
            session.closeNow();
        } else if (cause instanceof java.io.IOException) {
            Debug.SOCKET.warning("Сетевое исключение на соединении ID " + session.getId() + ": " + cause.getMessage());
            session.closeNow();
        } else {
            session.closeNow();
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        Debug.SOCKET.info("Простой соединения: ID = " + session.getId() + ", статус: " + status);
    }
}
