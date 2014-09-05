package fr.xephi.authme.api;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.Utils;
import fr.xephi.authme.cache.auth.PlayerAuth;
import fr.xephi.authme.cache.auth.PlayerCache;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.plugin.manager.CombatTagComunicator;
import fr.xephi.authme.security.PasswordSecurity;
import fr.xephi.authme.settings.Settings;

public class API {

    public static final String newline = System.getProperty("line.separator");
    public static AuthMe instance;
    public static DataSource database;

    public API(AuthMe instance, DataSource database) {
        API.instance = instance;
        API.database = database;
    }

    /**
     * Hook into AuthMe
     * 
     * @return AuthMe instance
     */
    public static AuthMe hookAuthMe() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("AuthMe");
        if (plugin == null || !(plugin instanceof AuthMe)) {
            return null;
        }
        return (AuthMe) plugin;
    }

    public AuthMe getPlugin() {
        return instance;
    }

    /**
     * 
     * @param player
     * @return true if player is authenticate
     */
    public static boolean isAuthenticated(Player player) {
        return PlayerCache.getInstance().isAuthenticated(player);
    }

    /**
     * 
     * @param player
     * @return true if player is a npc
     */
    @Deprecated
    public boolean isaNPC(Player player) {
        if (instance.getCitizensCommunicator().isNPC(player, instance))
            return true;
        return CombatTagComunicator.isNPC(player);
    }

    /**
     * 
     * @param player
     * @return true if player is a npc
     */
    public boolean isNPC(Player player) {
        if (instance.getCitizensCommunicator().isNPC(player, instance))
            return true;
        return CombatTagComunicator.isNPC(player);
    }

    /**
     * 
     * @param player
     * @return true if the player is unrestricted
     */
    public static boolean isUnrestricted(Player player) {
        return Utils.getInstance().isUnrestricted(player);
    }

    public static Location getLastLocation(Player player) {
        try {
            PlayerAuth auth = PlayerCache.getInstance().getAuth(player);

            if (auth != null) {
                Location loc = new Location(Bukkit.getWorld(auth.getWorld()), auth.getQuitLocX(), auth.getQuitLocY(), auth.getQuitLocZ());
                return loc;
            } else {
                return null;
            }

        } catch (NullPointerException ex) {
            return null;
        }
    }

    public static void setPlayerInventory(Player player, ItemStack[] content,
            ItemStack[] armor) {
        try {
            player.getInventory().setContents(content);
            player.getInventory().setArmorContents(armor);
        } catch (NullPointerException npe) {
        }
    }

    /**
     * 
     * @param player
     * @return true if player is registered
     */
    public static boolean isRegistered(Player player) {
        return database.isAuthAvailable(player.getUniqueId());
    }

    /**
     * @param String
     *            playerName, String passwordToCheck
     * @return true if the password is correct , false else
     */
    public static boolean checkPassword(Player player,
            String passwordToCheck) {
        if (!isRegistered(player))
            return false;
        PlayerAuth auth = database.getAuth(player.getUniqueId());
        try {
            return PasswordSecurity.comparePasswordWithHash(passwordToCheck, auth.getHash(), player.getName());
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    /**
     * Register a player
     * 
     * @param String
     *            playerName, String password
     * @return true if the player is register correctly
     */
    public static boolean registerPlayer(Player player, String password, UUID uuid) {
        try {
            String hash = PasswordSecurity.getHash(Settings.getPasswordHash, password, player.getName());
            if (isRegistered(player)) {
                return false;
            }
            PlayerAuth auth = new PlayerAuth(player.getName(), hash, "198.18.0.1", 0, "your@email.com", uuid);
            if (!database.saveAuth(auth)) {
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException ex) {
            return false;
        }
    }

    /**
     * Force a player to login
     * 
     * @param Player
     *            player
     */
    public static void forceLogin(Player player) {
        instance.management.performLogin(player, "dontneed", true);
    }

}
