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
    private HashMap<String, Prefix> ownedPrefixes; // 拥有的称号，包含默认称号
    private Prefix currentPrefix; // 当前使用的称号，包含默认称号

    public PrefixPlayer(Player player) {
        this.player = player;
        this.playerDataFile = new File(plugin.getDataFolder() + File.separator + "playerdata", player.getName().toLowerCase() + ".yml");
        this.playerDataYml = YamlConfiguration.loadConfiguration(playerDataFile);

        load();
    }

    public Player getBukkitPlayer() {
        return player;
    }

    public boolean exists() {
        return playerDataFile.exists();
    }

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
        Prefix defaultPrefix = new Prefix(plugin.getSettings().getDefaultPrefixName(), 0, true);

        // 考虑项不存在的可能
        if (playerDataYml.isConfigurationSection("owned_prefixes")) {
            Set<String> ownedPrefixesKeys = playerDataYml.getConfigurationSection("owned_prefixes").getKeys(false);

            if (ownedPrefixesKeys != null) {
                for (String prefixName : ownedPrefixesKeys) {
                    ownedPrefixes.put(prefixName, new Prefix(prefixName, getPrefixExpiredTime(prefixName)));
                }
            }
        }

        // 把默认的称号存进拥有的称号里
        ownedPrefixes.put(defaultPrefix.getPrefixName(), defaultPrefix);

        String tmp = playerDataYml.getString("current_prefix");

        // 没有其他称号当前称号就是默认称号
        this.currentPrefix = tmp == null ? defaultPrefix : new Prefix(tmp, getPrefixExpiredTime(tmp));
    }

    // 从拥有的称号里根据名字获得称号
    public Prefix getPrefixFromOwnedPrefixes(String prefixName) {
        return ownedPrefixes.get(prefixName);
    }

    // 没有则返回默认称号
    public List<Prefix> getOwnedPrefixes() {
        return new ArrayList<>(ownedPrefixes.values());
    }

    // 没有则返回默认称号
    public Prefix getCurrentPrefix() {
        return currentPrefix;
    }

    // 设置当前称号，NULL或有defaultPrefix标记的Prefix即为默认称号
    public boolean setCurrentPrefix(Prefix prefix) {
        if (prefix != null && !ownedPrefixes.containsValue(prefix)) {
            throw new IllegalArgumentException("prefix not owned!");
        }

        // 设置成null再重新载入当前称号即为默认称号
        playerDataYml.set("current_prefix", (prefix == null || prefix.isDefaultPrefix()) ? null : prefix.getPrefixName());

        if (save()) {
            load();
            // vault更新称号
            plugin.getVaultChat().setPlayerPrefix(player, getCurrentPrefix().getPrefixName());

            return true;
        }

        return false;
    }

    // 给予永久称号
    public boolean givePrefix(String prefixName) {
        return givePrefix(prefixName, 0);
    }

    // 给予或续费限时称号；如果已经是永久称号了再给予非0的过期时间会覆盖过期时间
    public boolean givePrefix(String prefixName, long expiredTime) {
        if (prefixName == null) {
            throw new IllegalArgumentException("prefix cannot be null!");
        }

        // 过期时间必须大于现在的时间
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
    public boolean takePrefix(Prefix prefix) {
        // 不能拿走自己没有的称号
        if (!ownedPrefixes.containsValue(prefix)) {
            throw new IllegalArgumentException("prefix not owned!");
        }

        // 不能拿走默认称号
        if (prefix.isDefaultPrefix()) {
            throw new IllegalArgumentException("default prefix cannot take!");
        }

        // 删除称号项
        playerDataYml.set("owned_prefixes." + prefix.getPrefixName(), null);

        // 如果当前用的称号和被取消的称号相同则恢复到默认称号(NULL)；equals()和hashcode()已被重写
        if (currentPrefix.equals(prefix)) {
            setCurrentPrefix(null);
        }

        if (save()) {
            load();

            return true;
        }

        return false;
    }

    // 得到过期时间
    private long getPrefixExpiredTime(String prefixName) {
        return playerDataYml.getLong("owned_prefixes." + prefixName + ".expired_time", -1);
    }
}
