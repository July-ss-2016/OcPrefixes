package vip.ourcraft.mcserverplugins.ocprefixes;

import org.bukkit.entity.Player;
import vip.ourcraft.mcserverplugins.ocprefixes.OcPrefixes;
import vip.ourcraft.mcserverplugins.ocprefixes.PrefixPlayer;

import java.util.HashMap;

public class PrefixManager {
    private HashMap<String, PrefixPlayer> prefixPlayerMap;

    public PrefixManager(OcPrefixes plugin) {
        this.prefixPlayerMap = new HashMap<>();
    }

    public PrefixPlayer getPrefixPlayer(Player player) {
        if (!Util.isPlayerOnline(player)) {
            throw new IllegalArgumentException("player must be online!");
        }

        String playerName = player.getName();

        if (!prefixPlayerMap.containsKey(playerName)) {
            prefixPlayerMap.put(playerName, new PrefixPlayer(player));
        }

        return prefixPlayerMap.get(playerName);
    }

    public void unloadAllPrefixPlayers() {
        prefixPlayerMap.clear();
    }
}
