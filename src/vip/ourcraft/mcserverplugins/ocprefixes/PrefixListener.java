package vip.ourcraft.mcserverplugins.ocprefixes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class PrefixListener implements Listener {
    private Plugin plugin;
    private PrefixManager prefixManager;

    public PrefixListener(OcPrefixes plugin) {
        this.plugin = plugin;
        this.prefixManager = plugin.getPrefixManager();
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // 更新当前称号
            if (player.isOnline()) {
                prefixManager.getPrefixPlayer(event.getPlayer()).updateCurrentPrefix();
            }
        }, 10L);
    }
}
