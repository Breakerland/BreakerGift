package fr.breakerland.breakergift;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import fr.breakerland.breakergift.command.AdminGiftCommand;
import fr.breakerland.breakergift.command.GiftCommand;
import fr.breakerland.breakergift.listener.InteractListener;
import net.md_5.bungee.api.ChatColor;

public class BreakerGift extends JavaPlugin {
	public Map<String, List<ItemStack>> gifts = new HashMap<>();
	public final List<Location> chests = new ArrayList<>();
	public final Set<UUID> admins = new HashSet<>();
	private Date date;
	private File dataFile;
	private FileConfiguration data;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		loadData();

		AdminGiftCommand adminCommand;
		getCommand("giftadmin").setExecutor(adminCommand = new AdminGiftCommand(this));
		getCommand("giftadmin").setTabCompleter(adminCommand);
		getCommand("gift").setExecutor(new GiftCommand(this));

		try {
			date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(getConfig().getString("date", "25/12/2019 00:00:00"));
		} catch (ParseException e) {
			e.printStackTrace();
			getLogger().warning("Impossible to parse the christmas date.");
		}

		getServer().getPluginManager().registerEvents(new InteractListener(this), this);
	}

	@Override
	public void onDisable() {
		data.set("chests", chests);
		gifts.forEach((k, v) -> data.set("players." + k.toString(), v.isEmpty() || v == null ? "none" : v));

		saveData();
	}

	public String getMessage(String key, String def) {
		return parseColors(getConfig().getString(key, def).replaceFirst("%prefix%", "&f[&6Gift&f]"));
	}

	public Date getDate() {
		return date;
	}

	public String parseColors(String str) {
		return ChatColor.translateAlternateColorCodes('&', str);
	}

	public FileConfiguration getData() {
		return data;
	}

	private void saveData() {
		try {
			data.save(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void formatItem(ItemStack item, String sender) {
		ItemMeta meta = item.getItemMeta();
		List<String> lores = new ArrayList<>();
		List<String> newLores = getConfig().getStringList("itemLores");
		for (String lore : newLores)
			if (lore.equalsIgnoreCase("%lores%")) {
				if (meta.hasLore())
					lores.addAll(meta.getLore());
			} else
				lores.add(parseColors(lore.replaceFirst("%player%", sender)));

		meta.setLore(lores);
		item.setItemMeta(meta);
	}

	private void loadData() {
		dataFile = new File(getDataFolder(), "data");
		if (!dataFile.exists())
			try {
				dataFile.getParentFile().mkdirs();
				dataFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

		data = new YamlConfiguration();
		try {
			data.load(dataFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		// LOAD PLAYER DATAS
		ConfigurationSection playerSection = data.getConfigurationSection("players");
		if (playerSection != null)
			for (String id : playerSection.getKeys(false))
				try {
					gifts.put(id, (List<ItemStack>) playerSection.getList(id));
				} catch (IllegalArgumentException e) {}

		// LOAD CHEST DATAS
		chests.addAll((List<Location>) data.getList("chests", new ArrayList<Location>()));
	}

	public boolean isChristmas() {
		return System.currentTimeMillis() >= date.getTime();
	}
}