package fr.xephi.authme.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.cache.auth.PlayerAuth;
import fr.xephi.authme.cache.auth.PlayerCache;

public class CacheDataSource implements DataSource {

    private DataSource source;
    public AuthMe plugin;
    private HashMap<UUID, PlayerAuth> cache = new HashMap<UUID, PlayerAuth>();

    public CacheDataSource(AuthMe plugin, DataSource source) {
        this.plugin = plugin;
        this.source = source;
    }

    @Override
    public synchronized boolean isAuthNameAvailable(String user) {
        if (cache.containsKey(user))
            return true;
        return source.isAuthNameAvailable(user);
    }

    @Override
    public synchronized boolean isAuthAvailable(UUID user) {
        if (cache.containsKey(user))
            return true;
        return source.isAuthAvailable(user);
    }

    @Override
    public synchronized PlayerAuth getNameAuth(String user) {
        if (cache.containsKey(user)) {
            return cache.get(user);
        } else {
            PlayerAuth auth = source.getNameAuth(user);
            if (auth != null)
                cache.put(auth.getUUID(), auth);
            return auth;
        }
    }

    @Override
    public synchronized PlayerAuth getAuth(UUID user) {
        if (cache.containsKey(user)) {
            return cache.get(user);
        } else {
            PlayerAuth auth = source.getAuth(user);
            if (auth != null)
                cache.put(user, auth);
            return auth;
        }
    }

    @Override
    public synchronized boolean saveAuth(PlayerAuth auth) {
        if (source.saveAuth(auth)) {
            cache.put(auth.getUUID(), auth);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean updatePassword(PlayerAuth auth) {
        if (source.updatePassword(auth)) {
            if (cache.containsKey(auth.getUUID()))
                cache.get(auth.getUUID()).setHash(auth.getHash());
            return true;
        }
        return false;
    }

    @Override
    public boolean updateSession(PlayerAuth auth) {
        if (source.updateSession(auth)) {
            if (cache.containsKey(auth.getUUID())) {
                cache.get(auth.getUUID()).setIp(auth.getIp());
                cache.get(auth.getUUID()).setLastLogin(auth.getLastLogin());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean updateQuitLoc(PlayerAuth auth) {
        if (source.updateQuitLoc(auth)) {
            if (cache.containsKey(auth.getUUID())) {
                cache.get(auth.getUUID()).setQuitLocX(auth.getQuitLocX());
                cache.get(auth.getUUID()).setQuitLocY(auth.getQuitLocY());
                cache.get(auth.getUUID()).setQuitLocZ(auth.getQuitLocZ());
                cache.get(auth.getUUID()).setWorld(auth.getWorld());
            }
            return true;
        }
        return false;
    }

    @Override
    public int getIps(String ip) {
        return source.getIps(ip);
    }

    @Override
    public int purgeDatabase(long until) {
        int cleared = source.purgeDatabase(until);
        if (cleared > 0) {
            for (PlayerAuth auth : cache.values()) {
                if (auth.getLastLogin() < until) {
                    cache.remove(auth.getUUID());
                }
            }
        }
        return cleared;
    }

    @Override
    public List<String> autoPurgeDatabase(long until) {
        List<String> cleared = source.autoPurgeDatabase(until);
        if (cleared.size() > 0) {
            for (PlayerAuth auth : cache.values()) {
                if (auth.getLastLogin() < until) {
                    cache.remove(auth.getUUID());
                }
            }
        }
        return cleared;
    }

    @Override
    public synchronized boolean removeAuth(UUID user) {
        if (source.removeAuth(user)) {
            cache.remove(user);
            return true;
        }
        return false;
    }

    @Override
    public synchronized void close() {
        source.close();
    }

    @Override
    public void reload() {
        cache.clear();
        source.reload();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID user = player.getUniqueId();
            if (PlayerCache.getInstance().isAuthenticated(player)) {
                try {
                    PlayerAuth auth = source.getAuth(user);
                    cache.put(user, auth);
                } catch (NullPointerException npe) {
                }
            }
        }
    }

    @Override
    public synchronized boolean updateEmail(PlayerAuth auth) {
        if (source.updateEmail(auth)) {
            if (cache.containsKey(auth.getUUID()))
                cache.get(auth.getUUID()).setEmail(auth.getEmail());
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean updateSalt(PlayerAuth auth) {
        if (source.updateSalt(auth)) {
            if (cache.containsKey(auth.getUUID()))
                cache.get(auth.getUUID()).setSalt(auth.getSalt());
            return true;
        }
        return false;
    }

    @Override
    public synchronized List<String> getAllAuthsByName(PlayerAuth auth) {
        return source.getAllAuthsByName(auth);
    }

    @Override
    public synchronized List<String> getAllAuthsByIp(String ip) {
        return source.getAllAuthsByIp(ip);
    }

    @Override
    public synchronized List<String> getAllAuthsByEmail(String email) {
        return source.getAllAuthsByEmail(email);
    }

    @Override
    public synchronized void purgeBanned(List<String> banned) {
        source.purgeBanned(banned);
        for (PlayerAuth auth : cache.values()) {
            if (banned.contains(auth.getUUID())) {
                cache.remove(auth.getUUID());
            }
        }
    }

    @Override
    public DataSourceType getType() {
        return source.getType();
    }

    @Override
    public boolean isLogged(UUID user) {
        return source.isLogged(user);
    }

    @Override
    public void setLogged(UUID user) {
        source.setLogged(user);
    }

    @Override
    public void setUnlogged(UUID user) {
        source.setUnlogged(user);
    }

    @Override
    public void purgeLogged() {
        source.purgeLogged();
    }

    @Override
    public int getAccountsRegistered() {
        return source.getAccountsRegistered();
    }

    @Override
    public void updateName(String oldone, String newone, UUID uuid) {
        if (cache.containsKey(uuid))
            cache.get(uuid).setName(newone);
        source.updateName(oldone, newone, uuid);
    }

    @Override
    public List<PlayerAuth> getAllAuths() {
        return source.getAllAuths();
    }

    @Override
    public boolean updateUUID(PlayerAuth auth) {
        if (source.updateUUID(auth)) {
            if (cache.containsKey(auth.getUUID()))
                cache.get(auth.getUUID()).setUUID(auth.getUUID());
            return true;
        }
        return false;
    }

    @Override
    public List<String> getAllPlayersByUUID(UUID uuid) {
        return source.getAllPlayersByUUID(uuid);
    }
}
