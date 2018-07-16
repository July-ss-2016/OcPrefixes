package vip.ourcraft.mcserverplugins.ocprefixes;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Util {
    public static void sendMsg(CommandSender cs, String msg) {
        cs.sendMessage("§a[OcPrefixes] §d" + ChatColor.translateAlternateColorCodes('&', msg));
    }

    public static void sendMsgWithoutPrefix(CommandSender cs, String msg) {
        cs.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public static boolean isPlayer(Object o) {
        return o instanceof Player;
    }

    public static boolean isPlayerOnline(Player player) {
        return player != null && player.isOnline();
    }
}
