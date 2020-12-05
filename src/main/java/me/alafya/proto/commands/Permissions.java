package me.alafya.proto.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.ServerOperator;

import me.alafya.proto.Main;

// TODO: better understand how permissions work
public class Permissions implements CommandExecutor {

    private Map<String, PermissionAttachment> permissions;
    Main plugin;

    public Permissions(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("perm").setExecutor(this);
        permissions = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equals("perm"))
            return false;
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender))
            return false;
        if (!(sender instanceof ServerOperator) || !((ServerOperator)sender).isOp()) {
            sender.sendMessage("Error: only server ops can use this command");
            return false;
        }
        if (args.length > 0 && args[0].equals("list")) {
            listPermissions(sender);
            return true;
        }
        if (args.length != 3) {
            sender.sendMessage("Error: usage /perm <username> <add/remove> <permission>");
            sender.sendMessage("Error: please enter the 3 required arguments");
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("Error: usage /perm <username> <add/remove> <permission>");
            sender.sendMessage("Error: invalid username");
        }

        switch (args[1]) {
            case "add":
                addPermission(sender, target, args[2]);
                break;
            case "remove":
                removePermission(sender, target, args[2]);
                break;
            default:
                sender.sendMessage("Error: usage /perm <username> <add/remove> <permission>");
                sender.sendMessage("Error: second argument must be add or remove");
                return false;
        }

        return true;
    }

    private void listPermissions(CommandSender sender) {
        for (Map.Entry<String, PermissionAttachment> permission : permissions.entrySet())
           sender.sendMessage(permission.getKey()); 
    }

    private void addPermission(CommandSender sender, Player target, String permission) {
        if (!permissions.containsKey(permission))
            sender.sendMessage("Warning: this permission does not yet exist (you are now creating it)");
        
        target.addAttachment(plugin, permission, true);
    }

    private void removePermission(CommandSender sender, Player target, String permission) {
        if (!permissions.containsKey(permission)) {
            sender.sendMessage("Error: this permission does not exist");
            return;
        }
        if (!target.hasPermission(permission)) {
            sender.sendMessage("Error: " + target.getName() + " does not have this permission");
            return;
        }

        target.removeAttachment(permissions.get(permission));
    }

    public void loadData() {

    }

    public void saveData() {

    }
    
}
