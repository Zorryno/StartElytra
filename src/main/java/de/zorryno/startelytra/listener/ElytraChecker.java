package de.zorryno.startelytra.listener;

import de.zorryno.startelytra.StartElytra;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ElytraChecker implements Listener {
    private StartElytra plugin;
    private ItemStack elytra;
    private static HashMap<UUID, ItemStack> armorContend = new HashMap<>();
    private int timer;
    private double multiplyer = 1.3;

    public ElytraChecker(StartElytra plugin) {
        this.plugin = plugin;
        String displayName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("elytra.title"));
        List<String> lore = new ArrayList<>();
        for(String text : plugin.getConfig().getStringList("elytra.lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', text));
        }

        elytra = new ItemStack(Material.ELYTRA);
        ItemMeta itemMeta = elytra.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, false);
        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(lore);
        elytra.setItemMeta(itemMeta);

        timer = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            Location pos1 = plugin.getLocations()[0];
            Location pos2 = plugin.getLocations()[1];

            if(pos1 != null && pos2 != null) {
                for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
                    if(!player.getWorld().equals(pos1.getWorld()))
                        continue;

                    if (isBetween(player.getLocation(), pos1, pos2)) {
                        if(player.getInventory().getChestplate() == null || !player.getInventory().getChestplate().isSimilar(elytra))
                            replaceArmor(player);
                    } else {
                        if(player.getInventory().getChestplate() != null && player.getInventory().getChestplate().isSimilar(elytra) && !player.isGliding())
                            replaceElytra(player);
                    }
                }
            }
        }, 0, 1);
    }

    private boolean isBetween(Location location, Location pos1, Location pos2) {
        return isBetween(location.getX(), pos1.getX(), pos2.getX()) &&
                isBetween(location.getY(), pos1.getY(), pos2.getY()) &&
                isBetween(location.getZ(), pos1.getZ(), pos2.getZ());
    }

    private boolean isBetween(double number, double min, double max) {
        return (number >= min && number <= max) || (number <= min && number >= max);
    }

    @EventHandler
    public void onLanding(EntityToggleGlideEvent event) {
        if(event.isGliding() || !(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();

        if(player.getInventory().getChestplate() != null && player.getInventory().getChestplate().isSimilar(elytra))
            replaceElytra(player);
    }

    public void replaceElytra(Player player) {
        if(player.getInventory().getChestplate() == null || player.getInventory().getChestplate().isSimilar(elytra)) {
            player.getInventory().setChestplate(armorContend.get(player.getUniqueId()));
            armorContend.remove(player.getUniqueId());
        }
    }

    public void replaceArmor(Player player) {
        if(armorContend.containsKey(player.getUniqueId()))
            return;

        if(player.getInventory().getChestplate() != null && !player.getInventory().getChestplate().isSimilar(elytra))
            armorContend.put(player.getUniqueId(), player.getInventory().getChestplate());
        player.getInventory().setChestplate(elytra);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        replaceElytra(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getCurrentItem() != null && event.getCurrentItem().isSimilar(elytra)) {
            event.setCancelled(true);
            return;
        }
    }

    public static void save(Plugin plugin) {
        for(UUID uuid : armorContend.keySet()) {
            plugin.getConfig().set("armor." + uuid.toString() + ".name", Bukkit.getOfflinePlayer(uuid).getName());
            plugin.getConfig().set("armor." + uuid.toString() + ".item", armorContend.get(uuid));
        }
        plugin.saveConfig();
    }

    public static void load(Plugin plugin) {
        long time = System.currentTimeMillis();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("armor");
        if(section == null)
            return;

        for(String key : section.getKeys(false)) {
            armorContend.put(UUID.fromString(key), section.getItemStack(key + ".item"));
        }
        plugin.getConfig().set("armor", null);
        plugin.saveConfig();
        time = System.currentTimeMillis() - time;
        plugin.getLogger().info("Armor loaded in " + time + " ms");
    }

    @EventHandler
    public void onBoost(PlayerSwapHandItemsEvent event) {
        if(!plugin.getConfig().getBoolean("boost"))
            return;

        Player player = event.getPlayer();

        if(player.getInventory().getChestplate() != null && player.getInventory().getChestplate().isSimilar(elytra) && player.isGliding()) {
            event.setCancelled(true);
            player.setVelocity(player.getEyeLocation().getDirection().multiply(multiplyer));
        }
    }
}
