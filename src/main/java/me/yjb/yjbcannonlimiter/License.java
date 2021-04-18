package me.yjb.yjbcannonlimiter;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class License
{
    private static File file;

    public static boolean setup()
    {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("YJBCannonLimiter").getDataFolder(), "license.txt");
        boolean fileCreated = false;

        if (!file.exists())
        {
            try
            {
                file.createNewFile();
                fileCreated = true;
            }
            catch (IOException e)
            {
                System.out.println("YJBCannonLimiter failed to create license.txt.");
            }
        }

        return fileCreated;
    }

    public static PrintStream getPrintStream()
    {
        try
        {
            return new PrintStream(file);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Scanner getScanner()
    {
        try
        {
            return new Scanner(file);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static File getFile() { return file; }
}
