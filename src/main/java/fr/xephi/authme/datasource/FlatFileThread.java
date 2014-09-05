package fr.xephi.authme.datasource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.cache.auth.PlayerAuth;
import fr.xephi.authme.settings.PlayersLogs;
import fr.xephi.authme.settings.Settings;

public class FlatFileThread extends Thread implements DataSource {

    /*
     * file layout:
     * 
     * PLAYERNAME:HASHSUM:IP:LOGININMILLIESECONDS:LASTPOSX:LASTPOSY:LASTPOSZ:
     * LASTPOSWORLD:EMAIL
     * 
     * Old but compatible:
     * PLAYERNAME:HASHSUM:IP:LOGININMILLIESECONDS:LASTPOSX:LASTPOSY
     * :LASTPOSZ:LASTPOSWORLD PLAYERNAME:HASHSUM:IP:LOGININMILLIESECONDS
     * PLAYERNAME:HASHSUM:IP PLAYERNAME:HASHSUM
     */
    private File source;

    public FlatFileThread() {
        source = new File(Settings.AUTH_FILE);
    }

    public void run() {
        try {
            source.createNewFile();
        } catch (IOException e) {
            ConsoleLogger.showError(e.getMessage());
            if (Settings.isStopEnabled) {
                ConsoleLogger.showError("Can't use FLAT FILE... SHUTDOWN...");
                AuthMe.getInstance().getServer().shutdown();
            }
            if (!Settings.isStopEnabled)
                AuthMe.getInstance().getServer().getPluginManager().disablePlugin(AuthMe.getInstance());
            return;
        }
    }

    @Override
    public synchronized boolean isAuthAvailable(String user) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 1 && args[0].equals(user)) {
                    return true;
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        return false;
    }

    @Override
    public synchronized boolean saveAuth(PlayerAuth auth) {
        if (isAuthAvailable(auth.getNickname())) {
            return false;
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(source, true));
            bw.write(auth.getNickname() + ":" + auth.getHash() + ":" + auth.getIp() + ":" + auth.getLastLogin() + ":" + auth.getQuitLocX() + ":" + auth.getQuitLocY() + ":" + auth.getQuitLocZ() + ":" + auth.getWorld() + ":" + auth.getEmail() + ":" + auth.getUUID().toString() + "\n");
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
        return true;
    }

    @Override
    public synchronized boolean updatePassword(PlayerAuth auth) {
        PlayerAuth newauth = this.getAuth(auth.getNickname());
        if (newauth == null) return false;
        removeAuth(newauth.getNickname());
        newauth.setHash(auth.getHash());
        return saveAuth(newauth);
    }

    @Override
    public synchronized boolean updateSession(PlayerAuth auth) {
        PlayerAuth newauth = this.getAuth(auth.getNickname());
        if (newauth == null) return false;
        removeAuth(newauth.getNickname());
        newauth.setIp(auth.getIp());
        newauth.setLastLogin(auth.getLastLogin());
        return saveAuth(newauth);
    }

    @Override
    public synchronized boolean updateQuitLoc(PlayerAuth auth) {
        PlayerAuth newauth = this.getAuth(auth.getNickname());
        if (newauth == null) return false;
        removeAuth(newauth.getNickname());
        newauth.setQuitLocX(auth.getQuitLocX());
        newauth.setQuitLocY(auth.getQuitLocY());
        newauth.setQuitLocZ(auth.getQuitLocZ());
        newauth.setWorld(auth.getWorld());
        return saveAuth(newauth);
    }

    @Override
    public synchronized int getIps(String ip) {
        BufferedReader br = null;
        int countIp = 0;
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 3 && args[2].equals(ip)) {
                    countIp++;
                }
            }
            return countIp;
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return 0;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return 0;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    @Override
    public synchronized int purgeDatabase(long until) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        ArrayList<String> lines = new ArrayList<String>();
        int cleared = 0;
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length >= 4) {
                    if (Long.parseLong(args[3]) >= until) {
                        lines.add(line);
                        continue;
                    }
                }
                cleared++;
            }
            bw = new BufferedWriter(new FileWriter(source));
            for (String l : lines) {
                bw.write(l + "\n");
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return cleared;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return cleared;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
        return cleared;
    }

    @Override
    public synchronized List<String> autoPurgeDatabase(long until) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        ArrayList<String> lines = new ArrayList<String>();
        List<String> cleared = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length >= 4) {
                    if (Long.parseLong(args[3]) >= until) {
                        lines.add(line);
                        continue;
                    }
                }
                cleared.add(args[0]);
            }
            bw = new BufferedWriter(new FileWriter(source));
            for (String l : lines) {
                bw.write(l + "\n");
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return cleared;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return cleared;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
        return cleared;
    }

    @Override
    public synchronized boolean removeAuth(String user) {
        if (!isAuthAvailable(user)) {
            return false;
        }
        BufferedReader br = null;
        BufferedWriter bw = null;
        ArrayList<String> lines = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 1 && !args[0].equals(user)) {
                    lines.add(line);
                }
            }
            bw = new BufferedWriter(new FileWriter(source));
            for (String l : lines) {
                bw.write(l + "\n");
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
        return true;
    }

    @Override
    public synchronized PlayerAuth getAuth(String user) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args[0].equals(user)) {
                    switch (args.length) {
                        case 2:
                            return new PlayerAuth(args[0], args[1], "198.18.0.1", 0, "your@email.com", new UUID(0,0));
                        case 3:
                            return new PlayerAuth(args[0], args[1], args[2], 0, "your@email.com", new UUID(0,0));
                        case 4:
                            return new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), "your@email.com", new UUID(0,0));
                        case 7:
                            return new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]), "unavailableworld", "your@email.com", new UUID(0,0));
                        case 8:
                            return new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]), args[7], "your@email.com", new UUID(0,0));
                        case 9:
                            return new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]), args[7], args[8], new UUID(0,0));
                        case 10:
                            return new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]), args[7], args[8], UUID.fromString(args[9]));
                        default:
                            return null;
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return null;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        return null;
    }

    @Override
    public synchronized void close() {
    }

    @Override
    public synchronized void reload() {
    }

    @Override
    public synchronized boolean updateEmail(PlayerAuth auth) {
        PlayerAuth newauth = this.getAuth(auth.getNickname());
        if (newauth == null) return false;
        removeAuth(newauth.getNickname());
        newauth.setEmail(auth.getEmail());
        return saveAuth(newauth);
    }

    @Override
    public synchronized boolean updateSalt(PlayerAuth auth) {
        return false;
    }

    @Override
    public synchronized List<String> getAllAuthsByName(PlayerAuth auth) {
        BufferedReader br = null;
        List<String> countIp = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 3 && args[2].equals(auth.getIp())) {
                    countIp.add(args[0]);
                }
            }
            return countIp;
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    @Override
    public synchronized List<String> getAllAuthsByIp(String ip) {
        BufferedReader br = null;
        List<String> countIp = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 3 && args[2].equals(ip)) {
                    countIp.add(args[0]);
                }
            }
            return countIp;
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    @Override
    public synchronized List<String> getAllAuthsByEmail(String email) {
        BufferedReader br = null;
        List<String> countEmail = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 8 && args[8].equals(email)) {
                    countEmail.add(args[0]);
                }
            }
            return countEmail;
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    @Override
    public synchronized void purgeBanned(List<String> banned) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        ArrayList<String> lines = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                try {
                    if (banned.contains(args[0])) {
                        lines.add(line);
                    }
                } catch (NullPointerException npe) {
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                }
            }
            bw = new BufferedWriter(new FileWriter(source));
            for (String l : lines) {
                bw.write(l + "\n");
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
        return;
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.FILE;
    }

    @Override
    public synchronized boolean isLogged(String user) {
        return PlayersLogs.getInstance().players.contains(user);
    }

    @Override
    public synchronized void setLogged(String user) {
        PlayersLogs.getInstance().addPlayer(user);
    }

    @Override
    public synchronized void setUnlogged(String user) {
        PlayersLogs.getInstance().removePlayer(user);
    }

    @Override
    public synchronized void purgeLogged() {
        PlayersLogs.getInstance().clear();
    }

    @Override
    public synchronized int getAccountsRegistered() {
        BufferedReader br = null;
        int result = 0;
        try {
            br = new BufferedReader(new FileReader(source));
            while ((br.readLine()) != null) {
                result++;
            }
        } catch (Exception ex) {
            ConsoleLogger.showError(ex.getMessage());
            return result;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        return result;
    }

    @Override
    public synchronized void updateName(String oldone, String newone) {
        PlayerAuth auth = this.getAuth(oldone);
        if (auth == null) return;
        auth.setName(newone);
        this.saveAuth(auth);
        this.removeAuth(oldone);
    }

    @Override
    public synchronized List<PlayerAuth> getAllAuths() {
        BufferedReader br = null;
        List<PlayerAuth> auths = new ArrayList<PlayerAuth>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                switch (args.length) {
                    case 2:
                        auths.add(new PlayerAuth(args[0], args[1], "198.18.0.1", 0, "your@email.com", new UUID(0,0)));
                    case 3:
                        auths.add(new PlayerAuth(args[0], args[1], args[2], 0, "your@email.com", new UUID(0,0)));
                    case 4:
                        auths.add(new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), "your@email.com", new UUID(0,0)));
                    case 7:
                        auths.add(new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]), "unavailableworld", "your@email.com", new UUID(0,0)));
                    case 8:
                        auths.add(new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]), args[7], "your@email.com", new UUID(0,0)));
                    case 9:
                        auths.add(new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]), args[7], args[8], new UUID(0,0)));
                    case 10:
                        auths.add(new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]), args[7], args[8], UUID.fromString(args[9])));
                    default: break;
                }
            }
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return auths;
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return auths;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
        return auths;
    }

    @Override
    public synchronized boolean updateUUID(PlayerAuth auth) {
        PlayerAuth newauth = this.getAuth(auth.getNickname());
        if (newauth == null) return false;
        removeAuth(newauth.getNickname());
        newauth.setUUID(auth.getUUID());
        return saveAuth(newauth);
    }

    @Override
    public synchronized List<String> getAllPlayersByUUID(UUID uuid) {
        BufferedReader br = null;
        List<String> count = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(":");
                if (args.length > 9 && args[9].equals(uuid.toString())) {
                    count.add(args[0]);
                }
            }
            return count;
        } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return new ArrayList<String>();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
