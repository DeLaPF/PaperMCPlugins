package me.alafya.proto.listeners;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import me.alafya.proto.Main;

public class VeinMiner implements Listener, CommandExecutor {

    private Set<Material> veins;
    private Set<UUID> hasVeinMinerEnabled;

    public VeinMiner(Main plugin) {
        plugin.getCommand("vein").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        veins = new HashSet<>(Arrays.asList(
            Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG,

            Material.COAL_ORE, Material.DIAMOND_ORE,
            Material.EMERALD_ORE, Material.GOLD_ORE,
            Material.IRON_ORE, Material.LAPIS_ORE,
            Material.REDSTONE_ORE,

            Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE, Material.ANCIENT_DEBRIS
        ));

        hasVeinMinerEnabled = new HashSet<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        if (args.length == 0) {
            sender.sendMessage("Proper use /vein <enable or disable>");
            return false;
        }
        
        UUID uuid = ((Player)sender).getUniqueId();
        switch (args[0]) {
            case "enable":
                sender.sendMessage("VeinMiner enabled");
                hasVeinMinerEnabled.add(uuid);
                break;
            case "disable":
                sender.sendMessage("disabled");
                hasVeinMinerEnabled.remove(uuid);
                break;
            default:
                sender.sendMessage("Proper use /vein <enable or disable>");
                return false;
        }

        return true;
    }

    public void loadData() {
        File file = new File("hasVeinMinerEnabled.ser");
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
            Set<UUID> temp = (Set<UUID>)ois.readObject();
            // does not allow for direct assigning to toggleVeinMiner for some reason?
            hasVeinMinerEnabled = temp;

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
            FileOutputStream fos = new FileOutputStream("hasVeinMinerEnabled.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(hasVeinMinerEnabled);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!e.getPlayer().isSneaking() || e.getPlayer() == null)
            return;

        UUID uuid = e.getPlayer().getUniqueId();

        // add players to hashmap
        if (!hasVeinMinerEnabled.contains(uuid)) {
            e.getPlayer().sendMessage("use /vein enable to enable VeinMiner");
            return;
        }

        // cancle normal block break event
        e.setCancelled(true);

        if (veins.contains(e.getBlock().getType()))
            handleVein(e);
        else
            handleAOE(e);
    }

    private void handleVein(BlockBreakEvent e) {
        Queue<Block> queue = new LinkedList<>();
        queue.add(e.getBlock());

        Material type = e.getBlock().getType();
        int blocksSearched = 0;
        Set<Block> visited = new HashSet<>();
        ItemStack tool =  e.getPlayer().getInventory().getItemInMainHand();

        while (!queue.isEmpty() && blocksSearched < 320) {
            Block curBlock = queue.remove();

            if (curBlock.getType() != type || visited.contains(curBlock))
                continue;

            if (tool.getItemMeta() instanceof Damageable && !damageTool(tool)) {
                e.getPlayer().sendMessage("Warning: Halting VeinMiner as item will break");
                return;
            }
            curBlock.breakNaturally(tool);

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
    }

    private void handleAOE(BlockBreakEvent e) {
        Material type = e.getBlock().getType();
        ItemStack tool =  e.getPlayer().getInventory().getItemInMainHand();

        BlockFace frontFace = e.getPlayer().getTargetBlockFace(8);
        BlockFace rightFace = BlockFace.EAST;
        BlockFace upFace = BlockFace.UP;
        BlockFace backFace = frontFace.getOppositeFace();

        boolean isUp = false;
        switch (frontFace) {
            case UP:
                isUp = true;
            case DOWN:
                float yaw = e.getPlayer().getLocation().getYaw();
                while (yaw < 0)
                    yaw += 360;
                yaw %= 360;

                if (yaw >= 315 || yaw < 45) { // South facing cone (SE to SW)
                    rightFace = BlockFace.WEST;
                    upFace = BlockFace.NORTH;
                }
                if (yaw >= 45 && yaw < 135) { // West facing cone (SW to NW)
                    rightFace = BlockFace.NORTH;
                    upFace = BlockFace.EAST;
                }
                if (yaw >= 135 && yaw < 225) { // North facing cone (NW to NE)
                    rightFace = BlockFace.EAST;
                    upFace = BlockFace.SOUTH;
                }
                if (yaw >= 225 && yaw < 315) { // East facing cone (NE to SE)
                    rightFace = BlockFace.SOUTH;
                    upFace = BlockFace.WEST;
                }

                // when looking at the top of a block your yaw is your relative "UP" direction
                if (isUp)
                    upFace = upFace.getOppositeFace();
                break;
            case NORTH:
                rightFace = BlockFace.WEST;
                break;
            case EAST:
                rightFace = BlockFace.NORTH;
                break;
            case SOUTH:
                rightFace = BlockFace.EAST;
                break;
            case WEST:
                rightFace = BlockFace.SOUTH;
                break;
            default:
                return;
        }

        // break 4 x 4 x 4 volume with get block as "bottom left block"
        Block curBlock = e.getBlock();
        for (int z = 0; z < 4; z++) {
            if (z != 0)
                curBlock = curBlock.getRelative(upFace);
            Block resetY = curBlock;

            for (int y = 0; y < 4; y++) {
                if (y != 0)
                    curBlock = curBlock.getRelative(backFace);
                Block resetX = curBlock;

                for (int x = 0; x < 4; x++) {
                    if (x != 0)
                        curBlock = curBlock.getRelative(rightFace);

                    if (curBlock.getType() == type) {
                        if (tool.getItemMeta() instanceof Damageable && !damageTool(tool)) {
                            e.getPlayer().sendMessage("Warning: Halting VeinMiner as item will break");
                            return;
                        }
                        curBlock.breakNaturally(tool);
                    }
                }

                curBlock = resetX;
            }

            curBlock = resetY;
        }
    }

    // TODO: Take into account unbreaking enchantment (unless its just added to dur)
    private boolean damageTool(ItemStack tool) {
        if (tool.getType().getMaxDurability() - ((Damageable)tool).getDamage() == 1)
            return false;

        int level = tool.getEnchantmentLevel(Enchantment.DURABILITY);
        Random rand = new Random();
        int damage = rand.nextInt(level) + 1 == 1 ? 1 : 0;
        
        ((Damageable)tool).setDamage(((Damageable)tool).getDamage() + damage);

        return true;
    }
    
}