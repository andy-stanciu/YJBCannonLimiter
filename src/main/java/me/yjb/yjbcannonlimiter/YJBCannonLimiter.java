package me.yjb.yjbcannonlimiter;

import me.yjb.yjbcannonlimiter.commands.Reload;
import me.yjb.yjbcannonlimiter.events.ExplodeEvent;
import me.yjb.yjbcannonlimiter.network.WebClient;
import me.yjb.yjbcannonlimiter.threads.TimerTask;
import me.yjb.yjbcannonlimiter.util.APStatus;
import me.yjb.yjbcannonlimiter.util.LocationStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.PrintStream;
import java.net.URI;
import java.util.*;

public final class YJBCannonLimiter extends JavaPlugin
{
    private final String version = "1.2.0";
    private final String product = "1";
    private final String server = "20.102.121.128:20500";
    private final long TIMEOUT = 5000;
    private PrintStream licenseOut;
    private Scanner licenseIn;
    private String hardwareID = null;
    private boolean valid = false;
    private String clientName = null;
    private String clientIP = null;

    private final String prefix = ChatColor.DARK_GRAY + "[" + getConfig().getString("lang.chat-prefix1") +
            getConfig().getString("lang.chat-prefix2") + ChatColor.DARK_GRAY + "] " + ChatColor.WHITE;
    private final String line = ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() +
            "--------------------------------------------------";

    private ArrayList<LocationStatus> recentLocations = new ArrayList<>();
    private ArrayList<APStatus> apLocations = new ArrayList<>();
    private BukkitTask garbageCollector;

    public ArrayList<LocationStatus> getRecentLocations() { return this.recentLocations; }
    public ArrayList<APStatus> getApLocations() { return this.apLocations; }
    public BukkitTask getGarbageCollector() { return this.garbageCollector; }
    public String getPrefix() { return color(this.prefix); }
    public String getLine() { return this.line; }
    public void setValid(boolean valid) { this.valid = valid; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setClientIP(String clientIP) { this.clientIP = clientIP; }

    private final Reload reload = new Reload(this);
    private final ExplodeEvent explodeEvent = new ExplodeEvent(this);

    public double speed;
    public int yThreshold;
    public List<String> enabledWorlds;
    public boolean isApAllowed;

    public final int AP_TIMEOUT = 5;
    public final int AP_Y_THRESHOLD = 1;
    public final int DISTANCE_THRESHOLD = 3;
    public final int AP_TRAVEL_THRESHOLD = 3;

    @Override
    public void onEnable()
    {
        this.valid = false;

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        boolean isLicensed = isIPRegistered();

        if (License.setup())
        {
            this.licenseOut = License.getPrintStream();
            this.licenseOut.println(this.clientIP);
        }
        else
        {
            this.licenseIn = License.getScanner();
            String currentIP = this.licenseIn.nextLine().trim();

            if (!this.clientIP.equals(currentIP))
            {
                this.licenseOut = License.getPrintStream();
                this.licenseOut.println(this.clientIP);
            }
        }

        if (!isLicensed)
        {
            System.out.println("[YJBCannonLimiter] Your server's IP is not whitelisted; disabling YJBCannonLimiter.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        System.out.println("[YJBCannonLimiter] Hello " + this.clientName + ", YJBCannonLimiter is starting up!");

        refreshConfigValues();

        Bukkit.getPluginManager().registerEvents(this.explodeEvent, this);

        getCommand("yjbcannonlimiterreload").setExecutor(this.reload);

        System.out.println("[YJBCannonLimiter] YJBCannonLimiter by yJb has started up.");
    }

    public void refreshConfigValues()
    {
        if (!getConfig().isSet("max-cannon-speed")) getConfig().set("max-cannon-speed", 3.0);
        if (!getConfig().isSet("one-stacker-threshold")) getConfig().set("one-stacker-threshold", 10);
        if (!getConfig().isSet("enabled-worlds")) getConfig().set("enabled-worlds", new String[] {"world"});
        if (!getConfig().isSet("allow-antipatch")) getConfig().set("allow-antipatch", true);

        saveConfig();

        this.speed = getConfig().getDouble("max-cannon-speed");
        this.yThreshold = getConfig().getInt("one-stacker-threshold");
        this.enabledWorlds = getConfig().getStringList("enabled-worlds");
        this.isApAllowed = getConfig().getBoolean("allow-antipatch");
    }

    public void initGarbageCollector()
    {
        this.garbageCollector = new TimerTask(this).runTaskTimer(this, 1L, 1L);
    }

    public String color(String text) { return ChatColor.translateAlternateColorCodes('&', text); }

    private boolean isIPRegistered()
    {
        System.out.println("[YJBCannonLimiter] Validating IP...");

        try
        {
            WebClient webClient = new WebClient(new URI("ws://" + this.server), this);
            webClient.connectBlocking();

            webClient.send("lic:" + this.product + "/" + this.version + "/" + Bukkit.getServer().getServerName() + "/" + getOperators());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        pauseThread();

        return this.valid;
    }

    private String getOperators()
    {
        Set<OfflinePlayer> players = Bukkit.getServer().getOperators();

        String operators = "";

        int i = 0;
        for (OfflinePlayer player : players)
        {
            if (i != 0) operators += ",";
            operators += player.getName();
            i++;
        }

        return operators;
    }

    private void pauseThread()
    {
        long currentTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - currentTime < TIMEOUT)
        {
            if (this.valid) return;
        }
    }
}
