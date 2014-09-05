package fr.xephi.authme.cache.auth;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

public class PlayerCache {

    private static PlayerCache singleton = null;
    private HashMap<UUID, PlayerAuth> cache;

    private PlayerCache() {
        cache = new HashMap<UUID, PlayerAuth>();
    }

    public void addPlayer(PlayerAuth auth) {
        cache.put(auth.getUUID(), auth);
    }

    public void updatePlayer(PlayerAuth auth) {
        cache.remove(auth.getUUID());
        cache.put(auth.getUUID(), auth);
    }

    public void removePlayer(Player player) {
        cache.remove(player.getUniqueId());
    }

    public boolean isAuthenticated(Player player) {
        return cache.containsKey(player.getUniqueId());
    }

    public PlayerAuth getAuth(Player player) {
        return cache.get(player.getUniqueId());
    }

    public static PlayerCache getInstance() {
        if (singleton == null) {
            singleton = new PlayerCache();
        }
        return singleton;
    }

    public int getLogged() {
        return cache.size();
    }

}
