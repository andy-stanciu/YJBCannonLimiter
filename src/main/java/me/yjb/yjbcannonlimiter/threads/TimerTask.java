package me.yjb.yjbcannonlimiter.threads;

import me.yjb.yjbcannonlimiter.YJBCannonLimiter;
import me.yjb.yjbcannonlimiter.util.APStatus;
import me.yjb.yjbcannonlimiter.util.LocationStatus;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Date;

public class TimerTask extends BukkitRunnable
{
    private final YJBCannonLimiter core;
    private final double BUFFER = 0.1;

    public TimerTask(YJBCannonLimiter core) { this.core = core; }

    @Override
    public void run()
    {
        ArrayList<LocationStatus> recentLocations = this.core.getRecentLocations();
        long now = new Date().getTime();
        Double secSpeed = (this.core.speed - BUFFER) * 1000;
        recentLocations.removeIf(l -> now - l.getCreated() > secSpeed.longValue());

        ArrayList<APStatus> apLocations = this.core.getApLocations();
        apLocations.removeIf(l -> MinecraftServer.currentTick - l.getCreated() > core.AP_TIMEOUT);
    }
}
