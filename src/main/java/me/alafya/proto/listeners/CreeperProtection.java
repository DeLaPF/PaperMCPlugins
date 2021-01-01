package me.alafya.proto.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Creeper;
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
        if (e.getEntity() instanceof Creeper)
            e.blockList().clear();
    }
}