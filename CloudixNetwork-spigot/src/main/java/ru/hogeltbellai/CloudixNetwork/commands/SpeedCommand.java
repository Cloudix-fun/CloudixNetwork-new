package ru.hogeltbellai.CloudixNetwork.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hogeltbellai.CloudixNetwork.api.commands.BaseCommand;
import ru.hogeltbellai.CloudixNetwork.api.commands.CommandInfo;
import ru.hogeltbellai.CloudixNetwork.utils.T;
import ru.hogeltbellai.CloudixNetwork.utils.U;

@CommandInfo(name = "speed", permission = "network.speed", forPlayer = true)
public class SpeedCommand extends BaseCommand {

    @Override
    protected boolean executeCommand(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            U.msg(sender, T.error("&#25B5FA&lCLOUDIX","Используйте - /" + label + " [скорость ходьбы/полёта]"));
            return true;
        }

        int speed;
        try {
            speed = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            U.msg(sender, T.system("&#25B5FA&lCLOUDIX", "Аргумент должен быть числом от 1 до 10"));
            return false;
        }

        if (speed < 1) speed = 1;
        if (speed > 10) speed = 10;
        if (player.isFlying()) {
            player.setFlySpeed(0.1F + 0.05F * (speed - 1));
            U.msg(sender, T.success("&#25B5FA&lCLOUDIX", "Скорость полёта установлена на: &f" + speed));
        } else {
            player.setWalkSpeed(0.2F + 0.08F * (speed - 1));
            U.msg(sender, T.success("&#25B5FA&lCLOUDIX", "Скорость ходьбы установлена на: &f" + speed));
        }

        return true;
    }
}