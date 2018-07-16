package vip.ourcraft.mcserverplugins.ocprefixes;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PrefixPlayer {
    private OcPrefixes plugin = OcPrefixes.getInstance();
    private Player player;
    private File playerDataFile;
    private YamlConfiguration playerDataYml;
    private HashMap<String, Prefix> ownedPrefixes; // 拥有的称号，不包含默认称号
    private Prefix currentPrefix; // 当前使用的称号，没有称号返回默认称号

    public PrefixPlayer(Player player) {
        this.player = player;
        this.playerDataFile = new File(plugin.getDataFolder() + File.separator + "playerdata", player.getName().toLowerCase() + ".yml");
        this.playerDataYml = YamlConfiguration.loadConfiguration(playerDataFile);

        if (exists()) {
            load();
        }
    }

    public Player getBukkitPlayer() {
        return player;
    }

    public boolean exists() {
        return playerDataFile.exists();
    }

/*    public boolean createNewPrefixPlayer() {
        try {
            if (playerDataFile.createNewFile()) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }*/

    public boolean save() {
        try {
            playerDataYml.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void load() {
        this.ownedPrefixes = new HashMap<>();
        this.currentPrefix = null;

        Set<String> ownedPrefixesKeys = playerDataYml.getConfigurationSection("owned_prefixes").getKeys(false);

        if (ownedPrefixesKeys != null) {
            for (String prefixName : ownedPrefixesKeys) {
                ownedPrefixes.put(prefixName, new Prefix(prefixName, getPrefixExpiredTime(prefixName)));
            }
        }

        String tmp = playerDataYml.getString("current_prefix");

        if (tmp != null) {
            currentPrefix = new Prefix(tmp, getPrefixExpiredTime(tmp));
        }
    }

    // 从拥有的称号里根据名字获得称号
    public Prefix getPrefixFromOwnedPrefixes(String prefixName) {
        return ownedPrefixes.get(prefixName);
    }

    // 没有则返回默认称号
    public List<Prefix> getOwnedPrefixes() {
        List<Prefix> resultList = ownedPrefixes == null ? new ArrayList<>() : new ArrayList<>(ownedPrefixes.values());

        resultList.add(new Prefix(plugin.getSettings().getDefaultPrefix(), 0, true)); // 添加个默认称号，需设置标记

        return resultList;
    }

    // 没有则返回默认称号
    public Prefix getCurrentPrefix() {
        return currentPrefix == null ? new Prefix(plugin.getSettings().getDefaultPrefix(), 0, true) : currentPrefix; // 没有就返回默认称号，需设置标记
    }

    // 设置当前称号
    public boolean setCurrentPrefix(String prefixName) {
        // null就是清除称号，使用默认称号
        if (prefixName != null && !ownedPrefixes.containsKey(prefixName)) {
            throw new IllegalArgumentException("prefix not owned!");
        }

        playerDataYml.set("current_prefix", prefixName);

        if (save()) {
            load();
            plugin.getVaultChat().setPlayerPrefix(player, prefixName == null ? plugin.getSettings().getDefaultPrefix() : prefixName);

            return true;
        }

        return false;
    }

    // 给予永久称号，会覆盖过期时间
    public boolean givePrefix(String prefixName) {
        return givePrefix(prefixName, 0);
    }

    // 给予限时称号，会覆盖过期时间
    public boolean givePrefix(String prefixName, long expiredTime) {
        if (prefixName == null) {
            throw new IllegalArgumentException("prefix cannot be null!");
        }

        if (expiredTime != 0 && System.currentTimeMillis() > expiredTime) {
            throw new IllegalArgumentException("expired time must > current time.");
        }

        playerDataYml.set("owned_prefixes." + prefixName + ".expired_time", expiredTime);

        if (save()) {
            load();

            return true;
        }

        return false;
    }

    // 拿走称号
    public boolean takePrefix(String prefixName) {
        if (!ownedPrefixes.containsKey(prefixName)) {
            throw new IllegalArgumentException("prefix not owned!");
        }

        playerDataYml.set("owned_prefixes." + prefixName, null);

        if (save()) {
            load();

            // 如果当前用的称号和被取消的称号相同则恢复到默认称号
            if (currentPrefix != null && currentPrefix.getPrefixName().equals(prefixName)) {
                setCurrentPrefix(null);
            }

            return true;
        }

        return false;
    }

    // 得到过期时间
    private long getPrefixExpiredTime(String prefixName) {
        return playerDataYml.getLong("owned_prefixes." + prefixName + ".expired_time", -1);
    }
}
