package fr.xephi.authme.datasource;

import java.util.List;
import java.util.UUID;

import fr.xephi.authme.cache.auth.PlayerAuth;

public interface DataSource {

    public enum DataSourceType {

        MYSQL,
        FILE,
        SQLITE
    }

    boolean isAuthAvailable(UUID uuid);

    boolean isAuthNameAvailable(String user);

    PlayerAuth getAuth(UUID uuid);

    PlayerAuth getNameAuth(String user);

    boolean saveAuth(PlayerAuth auth);

    boolean updateSession(PlayerAuth auth);

    boolean updatePassword(PlayerAuth auth);

    int purgeDatabase(long until);

    List<String> autoPurgeDatabase(long until);

    boolean removeAuth(UUID uuid);

    boolean updateQuitLoc(PlayerAuth auth);

    int getIps(String ip);

    List<String> getAllAuthsByName(PlayerAuth auth);

    List<String> getAllAuthsByIp(String ip);

    List<String> getAllAuthsByEmail(String email);

    boolean updateEmail(PlayerAuth auth);

    boolean updateSalt(PlayerAuth auth);

    void close();

    void reload();

    void purgeBanned(List<String> banned);

    DataSourceType getType();

    boolean isLogged(UUID uuid);

    void setLogged(UUID uuid);

    void setUnlogged(UUID uuid);

    void purgeLogged();

    int getAccountsRegistered();

    void updateName(String oldone, String newone, UUID uuid);

    List<PlayerAuth> getAllAuths();

    boolean updateUUID(PlayerAuth auth);

    List<String> getAllPlayersByUUID(UUID uuid);

}
