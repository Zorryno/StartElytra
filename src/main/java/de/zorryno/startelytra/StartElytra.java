package de.zorryno.startelytra;

import de.zorryno.startelytra.commands.ElytraCommand;
import de.zorryno.startelytra.listener.ElytraChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;


public final class StartElytra extends JavaPlugin {

    private volatile Location[] locations = new Location[2];
    private volatile static HashMap<UUID, ItemStack> armorContend = new HashMap<>();


    @Override
    public void onEnable() {
        ElytraCommand elytraCommand = new ElytraCommand(this);
        getCommand("elytra").setExecutor(elytraCommand);
        getCommand("elytra").setTabCompleter(elytraCommand);
        Bukkit.getPluginManager().registerEvents(new ElytraChecker(this), this);
        saveDefaultConfig();
        loadLocations();
        ElytraChecker.load(this);
        getLogger().info("Boosting: " + getConfig().getBoolean("boost"));
    }

    public boolean saveLocation(String position, Location location) {
        String pos = position.toLowerCase();
        if(!pos.equals("pos1") && !pos.equals("pos2"))
            return false;

        getConfig().set(pos, location);
        saveConfig();

        if(pos.equals("pos1")) {
            locations[0] = location;
        }
        if(pos.equals("pos2")) {
            locations[1] = location;
        }
        return true;
    }

    public void loadLocations() {
        long time = System.currentTimeMillis();
        reloadConfig();
        locations[0] = getConfig().getLocation("pos1");
        locations[1] = getConfig().getLocation("pos2");
        time = System.currentTimeMillis() - time;
        getLogger().info("Locations loaded in " + time + " ms");
    }
    @Override
    public void onDisable() {
        ElytraChecker.save(this);
    }

    public Location[] getLocations() {
        return locations;
    }
}
