package me.alafya.proto.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import me.alafya.proto.Main;

public class FindSetAndRemove implements CommandExecutor {

    private static Map<String, Location> locations;

    public FindSetAndRemove(Main plugin) {
        plugin.getCommand("find").setExecutor(this);
        plugin.getCommand("set").setExecutor(this);
        plugin.getCommand("remove").setExecutor(this);

        locations = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
        switch(lable) {
            case "find":
                return onFind(sender, cmd, lable, args);
            case "set":
                return onSet(sender, cmd, lable, args);
            case "remove":
                return onRemove(sender, cmd, lable, args);
        }

        return false;
    }

    // find location given name
    private boolean onFind(CommandSender sender, Command cmd, String lable, String[] args) {
        // return false if sender is not valid
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("Error: This command is only for players and consoles"); 
            return false;
        }

        // if no player is specified send player their location
        if (args.length == 0) {
            // if command is sent from console send spawn location 
            if (sender instanceof ConsoleCommandSender)
                sendLocation(sender, Bukkit.getWorlds().get(0).getSpawnLocation(), "Spawn");
            else // sender is innstaceof Player 
                sendLocation(sender, ((Player)sender).getLocation(), ((Player)sender).getName());

            return true;
        } 

        // handle args
        switch (args[0]) {
            case "spawn":
                sendLocation(sender, Bukkit.getWorlds().get(0).getSpawnLocation(), "Spawn");
                break;
            default:
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    sendLocation(sender, target.getLocation(), target.getName());
                }
                else if (locations.containsKey(args[0])) {
                    sendLocation(sender, locations.get(args[0]), args[0]);
                }
        }

        return true;
    }

    // add name and location to locations HashMap
    private boolean onSet(CommandSender sender, Command cmd, String lable, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Error: This command is only for players"); 
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("Error: Proper usage /set <nameoflocation>"); 
            return false;
        }

        if (Bukkit.getPlayer(args[0]) != null ||
            args[0].toLowerCase() == "spawn" ||
            locations.containsKey(args[0])) {
            sender.sendMessage("Error: Location with given name already exists");
            return false;
        }

        locations.put(args[0], ((Player)sender).getLocation());
        sender.sendMessage("Location Successfully created!");
        sendLocation(sender, ((Player)sender).getLocation(), args[0]);

        return true;
    }

    private boolean onRemove(CommandSender sender, Command cmd, String lable, String[] args) {
        // return false if sender is not valid
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("Error: This command is only for players and consoles"); 
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("Error: Proper usage /set <nameoflocation>"); 
            return false;
        }

        if (!locations.containsKey(args[0])) {
            sender.sendMessage("Error: This location does not exist");
            return false;
        }

        locations.remove(args[0]);

        return false;
    }

    // Send the formatted version of the provided location to the sender
    private void sendLocation(CommandSender sender, Location location, String name) {
        sender.sendMessage("Location of " + name + ": \n" + 
            "x: " + location.getBlockX() + " \n" +
            "y: " + location.getBlockY() + " \n" +
            "z: " + location.getBlockZ() + " \n");
    }

    public void loadData() {
        File file = new File("setLocations.ser");
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
            Map<String, Location> temp = deserialize((Map<String, Map<String, Object>>)ois.readObject());
            // does not allow for direct assigning to toggleVeinMiner for some reason?
            locations = temp;

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
            FileOutputStream fos = new FileOutputStream("setLocations.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(serialize());
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    private Map<String, Map<String, Object>> serialize() {
        Map<String, Map<String, Object>> serialized = new HashMap<>();
        for (Map.Entry<String, Location> entry : locations.entrySet())
            serialized.put(entry.getKey(), entry.getValue().serialize());

        return serialized;
    }

    private Map<String, Location> deserialize(Map<String, Map<String, Object>> serialized) {
        Map<String, Location> deserialized = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : serialized.entrySet())
            deserialized.put(entry.getKey(), Location.deserialize(entry.getValue()));
        
        return deserialized;
    }

}