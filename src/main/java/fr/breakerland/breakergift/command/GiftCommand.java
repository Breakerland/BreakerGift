package fr.breakerland.breakergift.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.breakerland.breakergift.BreakerGift;

public class GiftCommand implements CommandExecutor {

	private BreakerGift plugin;

	public GiftCommand(BreakerGift plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if (! (sender instanceof Player) || ! (args.length > 0) || args[0].equalsIgnoreCase("help"))
			return false;

		Player player = (Player) sender;
		OfflinePlayer receiver = Bukkit.getOfflinePlayer(args[0]);
		if (receiver == null)
			player.sendMessage(plugin.getMessage("playerNotFound", "%prefix% &cPlayer %player% doesn't exist!").replaceFirst("%player%", args[0]));
		else {
			ItemStack item = player.getInventory().getItemInMainHand();
			if (item.getType() == Material.AIR)
				player.sendMessage(plugin.getMessage("noItem", "%prefix% &cYou must have an item in your hand."));
			else {
				List<ItemStack> gifts = plugin.gifts.getOrDefault(receiver.getUniqueId().toString(), new ArrayList<ItemStack>());
				plugin.formatItem(item, player.getName());
				gifts.add(item);
				plugin.gifts.put(receiver.getUniqueId().toString(), gifts);
				player.sendMessage(plugin.getMessage("sendGift", "%prefix% &6You have sent a new gift to %player%").replaceFirst("%player%", receiver.getName()));
				if (receiver.isOnline())
					receiver.getPlayer().sendMessage(plugin.getMessage("receiveGift", "%prefix% &6You receive a new gift from %player%").replaceFirst("%player%", player.getName()));

				player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
			}
		}

		return true;
	}
}