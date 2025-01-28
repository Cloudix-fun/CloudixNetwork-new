package ru.hogeltbellai.CloudixNetwork.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.Debug;
import ru.hogeltbellai.CloudixNetwork.api.commands.BaseCommand;
import ru.hogeltbellai.CloudixNetwork.api.commands.CommandInfo;
import ru.hogeltbellai.CloudixNetwork.utils.S;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;
import ru.hogeltbellai.Core.packet.PacketGetPlayerInfo;
import ru.hogeltbellai.Core.packet.PacketPlayerInfo;

@CommandInfo(name = "stp", permission = "network.stp", forPlayer = true)
public class StpCommand extends BaseCommand {

    @Override
    protected boolean executeCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            U.msg(sender, T.error("&#B6DEA1&lCLOUDIX","Используйте - /" + label + " [игрок]"));
            return true;
        }

        String target = args[0];
        if (target.length() > 20) {
            U.msg(sender, T.error("&#B6DEA1&lCLOUDIX", "Некорректный ник"));
            return true;
        }

        PacketGetPlayerInfo request = new PacketGetPlayerInfo(target, 0);

        CNPluginSpigot.core().getCoreConnector().sendRequest(request, response -> {
            PacketPlayerInfo playerInfo = (PacketPlayerInfo) response;
            if (playerInfo.serverName != null && !playerInfo.serverName.isEmpty()) {
                U.msg(sender, T.system("&#B6DEA1&lCLOUDIX", "Вы телепортировались на сервер к " + target));
                connectToServer((Player) sender, playerInfo.serverName);
            } else {
                U.msg(sender, T.error("&#B6DEA1&lCLOUDIX", "Игрок " + target + " не в сети!"));
            }
        }, 200L);
        return true;
    }

    private void connectToServer(Player player, String serverName) {
        S.runAsync(() -> {
            try {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(serverName);
                player.sendPluginMessage(CNPluginSpigot.core(), "BungeeCord", out.toByteArray());
            } catch (Exception e) {
                Debug.CORE.logException("Ошибка при попытке подключения игрока к серверу: ", e);
                player.sendMessage("Не удалось подключиться к серверу. Пожалуйста, попробуйте снова.");
            }
        }, CNPluginSpigot.core());
    }
}
