package me.yjb.yjbcannonlimiter;

import me.yjb.yjbcannonlimiter.commands.Reload;
import me.yjb.yjbcannonlimiter.events.ExplodeEvent;
import me.yjb.yjbcannonlimiter.network.WebClient;
import me.yjb.yjbcannonlimiter.threads.TimerTask;
import me.yjb.yjbcannonlimiter.util.APStatus;
import me.yjb.yjbcannonlimiter.util.LocationStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.PrintStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public final class YJBCannonLimiter extends JavaPlugin
{
    private final String product = "YJBCannonLimiter";
    private final String server = "yjb.crystaldev.co";
    private final long TIMEOUT = 5000;
    private PrintStream licenseOut;
    private Scanner licenseIn;
    private String hardwareID = null;
    private boolean valid = false;
    private String clientName = null;

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

        this.hardwareID = getHwid();
        boolean isLicensed = isHwidRegistered();

        if (License.setup())
        {
            this.licenseOut = License.getPrintStream();
            this.licenseOut.println(this.hardwareID);
        }
        else
        {
            this.licenseIn = License.getScanner();
            String currentHwid = this.licenseIn.nextLine().trim();

            if (!this.hardwareID.equals(currentHwid))
            {
                this.licenseOut = License.getPrintStream();
                this.licenseOut.println(this.hardwareID);
            }
        }

        if (!isLicensed)
        {
            System.out.println("[YJBCannonLimiter] Your HWID is not licensed; disabling YJBCannonLimiter.");
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

    private boolean isHwidRegistered()
    {
        System.out.println("[YJBCannonLimiter] Verifying HWID...");

        try
        {
            WebClient webClient = new WebClient(new URI("ws://" + this.server), this);
            webClient.connectBlocking();
            webClient.send(this.product + "/" + this.hardwareID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        long currentTime = System.currentTimeMillis();

        while (!this.valid && System.currentTimeMillis() - currentTime < TIMEOUT) {}

        return this.valid;
    }

    private String getHwid()
    {
        try
        {
            String main = System.getenv("PROCESSOR_IDENTIFIER") + System.getProperty("user.name").trim() + System.getenv("COMPUTERNAME");
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            StringBuilder hwid = new StringBuilder();

            byte[] hash = messageDigest.digest(main.getBytes());

            for (int i = 0; i < hash.length; i++)
            {
                hwid.append(Integer.toHexString(hash[i] & 0xFF | 0x300).substring(0, 3));
                if (i != hash.length - 1) hwid.append("-");
            }

            return hwid.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
