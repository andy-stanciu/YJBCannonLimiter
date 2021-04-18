package me.yjb.yjbcannonlimiter.commands;

import me.yjb.yjbcannonlimiter.YJBCannonLimiter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Reload implements CommandExecutor
{
    private final YJBCannonLimiter core;

    public Reload(YJBCannonLimiter core) { this.core = core; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("yjbcannonlimiterreload") && sender instanceof Player)
        {
            Player p = (Player) sender;

            if (p.hasPermission("yjbcannonlimiter.reload"))
            {
                core.reloadConfig();
                core.refreshConfigValues();

                p.sendMessage(core.getLine());
                p.sendMessage(core.getPrefix() + "The config was reloaded!");
                p.sendMessage(core.getPrefix() + "Max cannon speed: " + ChatColor.GREEN + ((core.speed) == -1 ? "Unlimited" : core.speed));
                p.sendMessage(core.getPrefix() + "Anti-Patch cannons: " + ((core.isApAllowed) ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
                p.sendMessage(core.getLine());
            }
            else
            {
                p.sendMessage(core.getPrefix() + core.color(core.getConfig().getString("lang.no-perms")));
            }
        }
        return true;
    }
}
