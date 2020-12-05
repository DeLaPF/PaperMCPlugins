package me.alafya.proto;

import org.bukkit.plugin.java.JavaPlugin;

import me.alafya.proto.commands.FindAndSet;
import me.alafya.proto.commands.Permissions;
import me.alafya.proto.listeners.CreeperProtection;
import me.alafya.proto.listeners.Gravestone;
import me.alafya.proto.listeners.Parry;
import me.alafya.proto.listeners.VeinMiner;

public class Main extends JavaPlugin {

    Parry parry;
    FindAndSet findAndSet;
    Gravestone gravestone;
    VeinMiner veinMiner;
    Permissions permissions;

    // TODO: permissions plugin using serialization of HashMap<UUID, Set<String>>
    // where the set contains strings of permissions
    @Override
    public void onEnable() {
        super.onEnable();
        parry = new Parry(this);
        findAndSet = new FindAndSet(this);
        gravestone = new Gravestone(this);
        new CreeperProtection(this);
        veinMiner = new VeinMiner(this);
        permissions = new Permissions(this);
        loadData();
    } 

    @Override
    public void onDisable() {
        super.onDisable();
        saveData();
    }

    // read data from files and populate necessary structures
    // i.e. locations HashMap in FindAndSet
    private void loadData() {
        veinMiner.loadData();
        findAndSet.loadData();
        permissions.loadData();
    }

    private void saveData() {
        veinMiner.saveData();
        findAndSet.saveData();
        permissions.saveData();
    }
}