package vip.ourcraft.mcserverplugins.ocprefixes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.List;

public class PlayerCommand implements CommandExecutor {
    private OcPrefixes plugin;
    private SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private PrefixManager prefixManager;

    public PlayerCommand(OcPrefixes plugin) {
        this.plugin = plugin;
        this.prefixManager = plugin.getPrefixManager();
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        boolean isAdmin = cs.hasPermission("OcPrefixes.admin");

        // 个人信息
        if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
            if (!Util.isPlayer(cs)) {
                cs.sendMessage("命令执行者必须是玩家!");
                return true;
            }

            Player bukkitPlayer = (Player) cs;
            PrefixPlayer prefixPlayer = prefixManager.getPrefixPlayer(bukkitPlayer);

            Util.sendMsgWithoutPrefix(bukkitPlayer, getPrefixesMsg(prefixPlayer));
            return true;
        }

        // 切换称号
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            if (!Util.isPlayer(cs)) {
                cs.sendMessage("命令执行者必须是玩家!");
                return true;
            }

            int index;

            try {
                index = Integer.parseInt(args[1]);
            } catch (Exception e) {
                Util.sendMsg(cs, "&c序号必须是合法数字!");
                return true;
            }

            Player bukkitPlayer = (Player) cs;
            PrefixPlayer prefixPlayer = prefixManager.getPrefixPlayer(bukkitPlayer);
            List<Prefix> ownedPrefixes = prefixPlayer.getOwnedPrefixes();

            if (index > ownedPrefixes.size()) {
                Util.sendMsg(cs, "&c您只有 &e" + ownedPrefixes.size() + "个 &c称号, 而您输入的序号是 &e" + index + "&c, 输入 &e/prefix info &c来查看拥有的称号!");
                return true;
            }

            Prefix newCurrentPrefix = ownedPrefixes.get(index - 1);
            String newCurrentPrefixName = newCurrentPrefix.isDefaultPrefix() ? null : newCurrentPrefix.getPrefixName(); // 因为默认称号是不存储的，直接设置的话会产生not owned exception，而null是设置成默认称号正确方式

            if (prefixPlayer.setCurrentPrefix(newCurrentPrefixName)) {
                Util.sendMsg(cs, "&d成功将称号切换至: " + newCurrentPrefix.getPrefixName());
                return true;
            }

            Util.sendMsg(cs, "&c切换称号失败, 请联系管理员!");
            return true;
        }

        // 帮助信息
        Util.sendMsg(cs, "&c/prefix info &b- &c查看称号仓库");
        Util.sendMsg(cs, "&c/prefix set <序号> &b- &c切换称号");

        if (!isAdmin) {
            return true;
        }







        // 重载
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.loadConfig();
            prefixManager.unloadAllPrefixPlayers();
            cs.sendMessage("ok.");
            return true;
        }

        // 查看其它玩家的称号仓库
        if (args.length == 2 && args[0].equalsIgnoreCase("look")) {
            Player player = Bukkit.getPlayer(args[1]);

            if (!Util.isPlayerOnline(player)) {
                cs.sendMessage(args[1] + " 玩家不在线.");
                return true;
            }

            PrefixPlayer prefixPlayer = prefixManager.getPrefixPlayer(player);

            Util.sendMsgWithoutPrefix(cs, "\n" + player.getName() + "的称号仓库:\n" + getPrefixesMsg(prefixPlayer));
            return true;
        }

        // 给予称号
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            Player player = Bukkit.getPlayer(args[1]);
            String prefix = args[2];
            int day; // 0 = forever

            if (!Util.isPlayerOnline(player)) {
                cs.sendMessage(args[1] + " 玩家不在线.");
                return true;
            }

            try {
                day = Integer.parseInt(args[3]);
            } catch (Exception e) {
                cs.sendMessage(args[3] + " 天数不合法.");
                return true;
            }

            PrefixPlayer prefixPlayer = prefixManager.getPrefixPlayer(player);
            Prefix oldPrefix = prefixPlayer.getPrefixFromOwnedPrefixes(prefix);

            // 续费，不存在则创建
            prefixPlayer.givePrefix(prefix, day == 0 ? 0 : oldPrefix == null ? System.currentTimeMillis() + day * 86400000L : oldPrefix.getExpiredTime() + day * 86400000L);
            prefixPlayer.setCurrentPrefix(prefix);
            cs.sendMessage("ok.");
            return true;
        }

        // 拿走称号
        if (args.length == 3 && args[0].equalsIgnoreCase("take")) {
            Player player = Bukkit.getPlayer(args[1]);

            if (!Util.isPlayerOnline(player)) {
                cs.sendMessage(args[1] + " 玩家不在线.");
                return true;
            }

            PrefixPlayer prefixPlayer = prefixManager.getPrefixPlayer(player);
            List<Prefix> ownedPrefixes = prefixPlayer.getOwnedPrefixes();
            int index;

            try {
                index = Integer.parseInt(args[2]);
            } catch (Exception e) {
                cs.sendMessage("序号必须是合法数字!");
                return true;
            }

            if (index > ownedPrefixes.size()) {
                cs.sendMessage("序号越界!");
                return true;
            }

            Prefix prefix = ownedPrefixes.get(index - 1);

            if (prefix.isDefaultPrefix()) {
                cs.sendMessage("你不能拿走默认称号!");
                return true;
            }

            cs.sendMessage(prefixPlayer.takePrefix(prefix.getPrefixName()) ? "成功." : "失败.");
            return true;
        }


        cs.sendMessage("/prefix give <id> <prefix> <day[>=0]{0 = forever}>");
        cs.sendMessage("/prefix look <id>");
        cs.sendMessage("/prefix take <id> <index>");
        return true;
    }

    private String getPrefixesMsg(PrefixPlayer prefixPlayer) {
        Prefix currentPrefix = prefixPlayer.getCurrentPrefix();
        StringBuilder msg = new StringBuilder();
        int counter = 0;

        msg.append("&7======== &c称号仓库 &7========");
        msg.append("\n");

        for (Prefix prefix : prefixPlayer.getOwnedPrefixes()) {
            msg.append(prefix.equals(currentPrefix) ? "&d" : "&f").append(counter + 1).append(".").append(" &f").append(prefix.getPrefixName()).append(" &a-> &f").append(prefix.getExpiredTime() == 0 ? "永不过期" : SDF.format(prefix.getExpiredTime()) + " 到期");
            msg.append("\n");
            ++ counter;
        }

        return msg.toString();
    }
}
