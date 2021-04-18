package me.yjb.yjbcannonlimiter.util;

import org.bukkit.Location;

public class APStatus
{
    private Location location;
    private int created;

    public APStatus(Location location, int currentTick)
    {
        this.location = location;
        this.created = currentTick;
    }

    public Location getLocation() { return this.location; }
    public int getCreated() { return this.created; }
}
