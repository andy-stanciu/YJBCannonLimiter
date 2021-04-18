package me.yjb.yjbcannonlimiter.events;

import me.yjb.yjbcannonlimiter.YJBCannonLimiter;
import me.yjb.yjbcannonlimiter.util.APStatus;
import me.yjb.yjbcannonlimiter.util.LocationStatus;
import net.minecraft.server.v1_8_R3.EntityTNTPrimed;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTNTPrimed;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class ExplodeEvent implements Listener
{
    private final YJBCannonLimiter core;

    public ExplodeEvent(YJBCannonLimiter core)
    {
        this.core = core;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(EntityExplodeEvent e)
    {
        List<Block> removedBlocks = e.blockList();

        if (!removedBlocks.isEmpty() && e.getEntityType() == EntityType.PRIMED_TNT)
        {
            if (core.enabledWorlds.contains(e.getLocation().getWorld().getName()))
            {
                //TNT did or attempted to do damage
                Location l = e.getLocation();

                if (l.getBlockY() > core.yThreshold)
                {
                    if (!core.isApAllowed)
                    {
                        int currentTick = MinecraftServer.currentTick;
                        EntityTNTPrimed tntPrimed = ((CraftTNTPrimed) e.getEntity()).getHandle();

                        if (Math.abs(tntPrimed.lastX - l.getBlockX()) > core.AP_TRAVEL_THRESHOLD || Math.abs(tntPrimed.lastZ - l.getBlockZ()) > core.AP_TRAVEL_THRESHOLD)
                        {
                            //This is a tunnel (AP)
                            if (!containsAp(l))
                            {
                                core.getApLocations().add(new APStatus(l, currentTick));
                            }

                            e.setCancelled(true);
                            return;
                        }

                        if (containsAp(l))
                        {
                            //Cancelling later gameticks of AP
                            e.setCancelled(true);
                            return;
                        }
                    }

                    if (core.speed != -1)
                    {
                        if (!isSlabbust(removedBlocks))
                        {
                            if (explosionHandler(l))
                            {
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean containsAp(Location l)
    {
        for (APStatus apStatus : core.getApLocations())
        {
            if (apStatus.getLocation().getWorld() == l.getWorld()
                    && apStatus.getLocation().getBlockX() == l.getBlockX()
                    && (Math.abs(apStatus.getLocation().getBlockY() - l.getBlockY()) <= core.AP_Y_THRESHOLD)
                    && apStatus.getLocation().getBlockZ() == l.getBlockZ())
            {
                return true;
            }
        }
        return false;
    }

    private boolean explosionHandler(Location l)
    {
        boolean foundLocOrScatter = false;

        for (LocationStatus ls : core.getRecentLocations())
        {
            if (ls.getLocation().getWorld() == l.getWorld()
                    && ls.getLocation().getBlockX() == l.getBlockX()
                    && ls.getLocation().getBlockZ() == l.getBlockZ())
            {
                foundLocOrScatter = true;
                break;
            }
        }

        if (!foundLocOrScatter)
        {
            //Checks if there is a recentLocation in a 3 block radius
            //If not, then goes ahead and registers new location
            if (!checkNearby(l))
            {
                //Adds location which expires after 0.5 seconds and then gets deleted after specified seconds
                core.getRecentLocations().add(new LocationStatus(l));

                if (core.getGarbageCollector() == null)
                {
                    core.initGarbageCollector();
                }
                return false;
            }
            else
            {
                //Otherwise, cancels the explosion (method returns true)
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    private boolean checkNearby(Location l)
    {
        for (LocationStatus ls : core.getRecentLocations())
        {
            //Criteria to cancel explosion
            if (!ls.getStatus() && ls.getLocation().getWorld().equals(l.getWorld())
                    && Math.abs(ls.getLocation().getBlockX() - l.getBlockX()) < core.DISTANCE_THRESHOLD
                    && Math.abs(ls.getLocation().getBlockZ() - l.getBlockZ()) < core.DISTANCE_THRESHOLD)
            {
                return true;
            }
        }
        //Allows explosion
        return false;
    }

    private boolean isSlabbust(List<Block> blocks)
    {
        for (Block b : blocks)
        {
            if (Material.STEP == b.getType()) return true;
        }
        return false;
    }
}
