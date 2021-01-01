package me.alafya.proto;

import org.bukkit.plugin.java.JavaPlugin;

import me.alafya.proto.commands.FindSetAndRemove;
import me.alafya.proto.commands.Permissions;
import me.alafya.proto.listeners.CreeperProtection;
import me.alafya.proto.listeners.Gravestone;
import me.alafya.proto.listeners.Parry;
import me.alafya.proto.listeners.VeinMiner;

public class Main extends JavaPlugin {

    Parry parry;
    FindSetAndRemove findSetAndRemove;
    Gravestone gravestone;
    VeinMiner veinMiner;
    Permissions permissions;

    @Override
    public void onEnable() {
        super.onEnable();
        parry = new Parry(this);
        findSetAndRemove = new FindSetAndRemove(this);
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
        findSetAndRemove.loadData();
        gravestone.loadData();
        permissions.loadData();
    }

    private void saveData() {
        veinMiner.saveData();
        findSetAndRemove.saveData();
        gravestone.saveData();
        permissions.saveData();
    }
}