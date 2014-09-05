package fr.xephi.authme.task;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.cache.auth.PlayerCache;
import fr.xephi.authme.cache.limbo.LimboCache;

public class MessageTask implements Runnable {

    private AuthMe plugin;
    private Player player;
    private String[] msg;
    private int interval;

    public MessageTask(AuthMe plugin, Player player, String[] strings,
            int interval) {
        this.plugin = plugin;
        this.msg = strings;
        this.player = player;
        this.interval = interval;
    }

    @Override
    public void run() {
        if (PlayerCache.getInstance().isAuthenticated(player))
            return;

        for (String ms : msg) {
            player.sendMessage(ms);
        }
        BukkitScheduler sched = plugin.getServer().getScheduler();
        int late = sched.scheduleSyncDelayedTask(plugin, this, interval * 20);
        if (LimboCache.getInstance().hasLimboPlayer(player)) {
            LimboCache.getInstance().getLimboPlayer(player).setMessageTaskId(late);
        }
    }
}
