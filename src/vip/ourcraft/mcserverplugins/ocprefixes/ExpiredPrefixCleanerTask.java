package vip.ourcraft.mcserverplugins.ocprefixes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ExpiredPrefixCleanerTask implements Runnable {
    public PrefixManager prefixManager;

    public ExpiredPrefixCleanerTask(OcPrefixes plugin) {
        this.prefixManager = plugin.getPrefixManager();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isOnline()) {
                continue;
            }

            PrefixPlayer prefixPlayer = prefixManager.getPrefixPlayer(player);

            for (Prefix prefix : prefixPlayer.getOwnedPrefixes()) {
                long expiredTime = prefix.getExpiredTime();

                // 排除永久的
                if (expiredTime != 0 && System.currentTimeMillis() > expiredTime) {
                    prefixPlayer.takePrefix(prefix);
                    Util.sendMsg(player, "&c您的称号 &e" + prefix.getPrefixName() + " &c已到期!");
                }
            }
        }
    }
}
