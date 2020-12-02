package me.alafya.proto.listeners;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import me.alafya.proto.Main;

public class Parry implements Listener {
   
    @SuppressWarnings("unused")
    private Main plugin;

    public Parry(Main plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onProjectileCollision(ProjectileCollideEvent e) {
        Projectile projectile = e.getEntity();
        Entity collidedWith = e.getCollidedWith();
        
        if (!(projectile instanceof Arrow) ||
            !(collidedWith instanceof Player) ||
            !(projectile.getShooter() instanceof Entity)) {
                return;
        }
        
        Player p = (Player)collidedWith;
        Vector forwardVector = p.getFacing().getDirection();
        boolean isGrounded = p.getLocation()
                                .add(0.0, 0.95, 0.0)
                                .getBlock()
                                .getRelative(BlockFace.DOWN)
                                .getType() != Material.AIR; 

        Entity shooter = (Entity)projectile.getShooter();
        if (p.isBlocking() && !isGrounded) {
            e.setCancelled(true);
            p.teleport(shooter.getLocation().add(forwardVector.multiply(0)));
        }
    }
}