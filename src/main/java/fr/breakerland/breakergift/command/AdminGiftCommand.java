package fr.breakerland.breakergift.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import fr.breakerland.breakergift.BreakerGift;

public class AdminGiftCommand implements CommandExecutor, TabCompleter {
	private BreakerGift plugin;

	public AdminGiftCommand(BreakerGift plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if (! (sender instanceof Player) || args.length == 0)
			return false;

		Player player = (Player) sender;

		if (args[0].equalsIgnoreCase("modify")) {
			plugin.admins.add(player.getUniqueId());
			player.sendMessage(plugin.getMessage("modifyMode", "%prefix% &cClick on the chest that you want add or remove from gift chests."));
		} else if (args[0].equalsIgnoreCase("giveall")) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if (item.getType() == Material.AIR)
				player.sendMessage(plugin.getMessage("noItem", "%prefix% &cYou must have an item in your hand."));
			else {
				plugin.formatItem(item, "Staff");
				Map<String, List<ItemStack>> newGifts = new HashMap<>();
				for (Entry<String, List<ItemStack>> entry : plugin.gifts.entrySet())
					if (entry.getValue() != null) {
						List<ItemStack> items = entry.getValue();
						items.add(item);
						newGifts.put(entry.getKey(), items);
					}

				plugin.gifts = newGifts;
				player.sendMessage(plugin.getMessage("giveall", "%prefix% &6You have sent a new gift to all players."));
				Bukkit.broadcastMessage(plugin.getMessage("receiveGiveall", "%prefix% &6You receive a new gift from the server staff!"));

				player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
			}
		}

		return true;
	}

	private static final List<String> COMMANDS = Arrays.asList("modify", "help", "giveall");

	@Override
	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
		return args.length > 0 ? StringUtil.copyPartialMatches(args[0], COMMANDS, new ArrayList<String>()) : null;
	}
}
