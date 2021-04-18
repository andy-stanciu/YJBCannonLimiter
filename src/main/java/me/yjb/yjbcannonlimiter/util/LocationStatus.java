package me.yjb.yjbcannonlimiter.util;

import org.bukkit.Location;

import java.util.Date;

public class LocationStatus
{
    private final Location l;
    private long created;

    public LocationStatus(Location l)
    {
        this.l = l;
        this.created = new Date().getTime();
    }

    public Location getLocation() { return this.l; }
    public long getCreated() { return this.created; }

    //status, true if ready for another shot, false otherwise
    public boolean getStatus()
    {
        Long now = new Date().getTime();

        return now - this.created < 500;
    }
}
