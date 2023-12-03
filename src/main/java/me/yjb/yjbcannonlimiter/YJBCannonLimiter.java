package me.yjb.yjbcannonlimiter;

import me.yjb.yjbcannonlimiter.commands.Reload;
import me.yjb.yjbcannonlimiter.events.ExplodeEvent;
import me.yjb.yjbcannonlimiter.threads.TimerTask;
import me.yjb.yjbcannonlimiter.util.APStatus;
import me.yjb.yjbcannonlimiter.util.LocationStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;

public final class YJBCannonLimiter extends JavaPlugin
{
    private final String version = "1.3.0";

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
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        System.out.println("[YJBCannonLimiter] Hello Unlicensed User, YJBCannonLimiter is starting up!");

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
}
