package me.alafya.proto.listeners;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.alafya.proto.Main;

public class Gravestone implements Listener, Serializable {

    private static final long serialVersionUID = 8498773530816769289L;
    Map<Location, GravestoneContents> gravestones;

    public Gravestone(Main plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        gravestones = new HashMap<>();
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        ItemStack[] playerItems = p.getInventory().getContents();
        Block graveSpawnBlock = findSpawnableBlock(p.getLocation());

        graveSpawnBlock.setType(Material.PLAYER_HEAD);
        Skull skull = (Skull)graveSpawnBlock.getState();
        UUID uuid = p.getUniqueId();
        skull.setOwningPlayer(Bukkit.getServer().getOfflinePlayer(uuid));
        skull.update();

        GravestoneContents contents = new GravestoneContents(p, p.getExpToLevel());
        
        for (ItemStack item : playerItems)
            if (item != null) contents.getItems().add(item);

        gravestones.put(graveSpawnBlock.getLocation(), contents);
        e.getDrops().clear();
        e.setDroppedExp(0);
    }

    @EventHandler
    public void onGravestoneBreak(BlockBreakEvent e) {
        Block grave = e.getBlock();
        Location location = grave.getLocation();
        Player p = e.getPlayer();

        if (p == null || grave.getType() != Material.PLAYER_HEAD || !gravestones.containsKey(location))
            return;

        GravestoneContents contents = gravestones.get(location);

        for (ItemStack itemStack : contents.getItems())
            if (itemStack != null) p.getWorld().dropItemNaturally(location, itemStack);
        
        e.setExpToDrop(contents.getExp());

        // 10% chance to drop player skull
        Random rand = new Random();
        if (rand.nextInt(100) % 10 != 0)
            e.setDropItems(false);
    }

    private Block findSpawnableBlock(Location l) {
        Set<Material> spawnable = new HashSet<>(Arrays.asList(Material.AIR, Material.VOID_AIR, Material.CAVE_AIR));
        Block block = l.getBlock();
        if (block.getType() == Material.WATER || block.getType() == Material.LAVA) {
            while (!spawnable.contains(block.getType()))
                block = block.getLocation().add(0.0, 1.0, 0.0).getBlock();
            
            return block;
        }

        Queue<Block> queue = new LinkedList<>();
        queue.add(block);
        int blocksSearched = 0;
        Set<Block> visited = new HashSet<>();

        while (!queue.isEmpty() && blocksSearched < 320) {
            Block curBlock = queue.remove();

            if (visited.contains(curBlock)) continue;
            if (spawnable.contains(curBlock.getType()))
                return curBlock;

            // add all adjacent blocks to queue
            queue.add(curBlock.getRelative(BlockFace.UP));
            queue.add(curBlock.getRelative(BlockFace.DOWN));
            queue.add(curBlock.getRelative(BlockFace.NORTH));
            queue.add(curBlock.getRelative(BlockFace.SOUTH));
            queue.add(curBlock.getRelative(BlockFace.EAST));
            queue.add(curBlock.getRelative(BlockFace.WEST));

            // limit the number of blocks searched
            blocksSearched++;
            // ensure the same block isn't searched twice
            visited.add(curBlock);
        }

        l.getBlock().setType(Material.AIR);
        Bukkit.broadcastMessage("Error: Could not find location to place gravestone");
        return l.getBlock();
    }

    public void loadData() {
        File file = new File("gravestones.ser");
        if (!file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);

            // supressed because file should only be altered by this plugin
            // will warn on first run due to empty file
            @SuppressWarnings("unchecked")
            Map<Location, GravestoneContents> temp = deserialize((Map<Map<String,Object>, GravestoneContents>)ois.readObject());
            // does not allow for direct assigning to toggleVeinMiner for some reason?
            gravestones = temp;

            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
    }
    
    public void saveData() {
        try {
            FileOutputStream fos = new FileOutputStream("gravestones.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(serialize());
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private Map<Map<String, Object>, GravestoneContents> serialize() {
        Map<Map<String, Object>, GravestoneContents> serialized = new HashMap<>();
        for (Map.Entry<Location, GravestoneContents> entry : gravestones.entrySet())
            serialized.put(entry.getKey().serialize(), entry.getValue());

        return serialized;
    }

    private Map<Location, GravestoneContents> deserialize(Map<Map<String, Object>, GravestoneContents> serialized) {
        Map<Location, GravestoneContents> deserialized = new HashMap<>();
        for (Map.Entry<Map<String, Object>, GravestoneContents> entry : serialized.entrySet())
            deserialized.put(Location.deserialize(entry.getKey()), entry.getValue());
        
        return deserialized;
    }

    class GravestoneContents implements Serializable {

        private static final long serialVersionUID = 5295419285627241855L;
        private transient List<ItemStack> items;
        private int exp;
        private transient Player owner;

        public GravestoneContents(Player p, int exp) {
            items = new ArrayList<>();
            this.exp = exp;
            owner = p;
        }

        private List<ItemStack> getItems() {
            return items;
        }

        private int getExp() {
            return exp;
        }

        @SuppressWarnings("unused")
        private Player getOwner() {
            return owner;
        }

    }

}