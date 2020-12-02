package me.alafya.proto.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import me.alafya.proto.Main;

public class CreeperProtection implements Listener {
    
    public CreeperProtection(Main plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCreeperExposion(EntityExplodeEvent e) {
        e.blockList().clear();
    }
}