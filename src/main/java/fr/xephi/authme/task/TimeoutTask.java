package fr.xephi.authme.task;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.cache.auth.PlayerCache;
import fr.xephi.authme.cache.backup.FileCache;
import fr.xephi.authme.cache.limbo.LimboCache;
import fr.xephi.authme.cache.limbo.LimboPlayer;
import fr.xephi.authme.listener.AuthMePlayerListener;
import fr.xephi.authme.settings.Messages;

public class TimeoutTask implements Runnable {

    private AuthMe plugin;
    private Player player;
    private Messages m = Messages.getInstance();
    private FileCache playerCache;

    public TimeoutTask(AuthMe plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerCache = new FileCache(plugin);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void run() {
        if (PlayerCache.getInstance().isAuthenticated(player))
            return;

        if (LimboCache.getInstance().hasLimboPlayer(player)) {
            LimboPlayer inv = LimboCache.getInstance().getLimboPlayer(player);
            player.getServer().getScheduler().cancelTask(inv.getMessageTaskId());
            player.getServer().getScheduler().cancelTask(inv.getTimeoutTaskId());
            if (playerCache.doesCacheExist(player)) {
                playerCache.removeCache(player);
            }
        }
        GameMode gm = AuthMePlayerListener.gameMode.get(player);
        player.setGameMode(gm);
        ConsoleLogger.info("Set " + player.getName() + " to gamemode: " + gm.name());
        player.kickPlayer(m._("timeout")[0]);
    }
}
