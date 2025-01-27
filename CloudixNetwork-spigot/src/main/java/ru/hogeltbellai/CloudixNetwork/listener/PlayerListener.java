package ru.hogeltbellai.CloudixNetwork.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;
import ru.hogeltbellai.CloudixNetwork.impl.CPlayerManager;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if(CPlayerManager.isBanned(player.getName())) {
            event.setKickMessage(U.colored(T.bantitle(player.getName())));
            event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(!CNPluginSpigot.core().getMysqlPlayer().playerExists(player.getName())) {
            CNPluginSpigot.core().getMysqlPlayer().addPlayer(player.getName());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
    }
}
