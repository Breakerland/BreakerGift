package fr.breakerland.breakergift.listener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.breakerland.breakergift.BreakerGift;

public class InteractListener implements Listener {
	final String dateFormat;
	final Set<UUID> players;
	final BreakerGift plugin;

	public InteractListener(BreakerGift plugin) {
		this.plugin = plugin;

		dateFormat = new SimpleDateFormat(plugin.getConfig().getString("dateFormat", "dd MMMM yyyy")).format(plugin.getDate());
		players = new HashSet<>();
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (! (block.getType() == Material.CHEST || block.getType() == Material.ENDER_CHEST))
			return;

		Player player = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && plugin.chests.contains(block.getLocation())) {
			if (plugin.isChristmas()) {
				List<ItemStack> items = plugin.gifts.get(player.getUniqueId());
				if (items == null) {
					items = plugin.gifts.get("all");
					if (items != null)
						plugin.gifts.put(player.getUniqueId().toString(), items);
				}

				if (items == null || ! (items.size() > 0))
					player.sendMessage(plugin.getMessage("noGift", "&cYou don't have new gifts!"));
				else {
					Inventory inventory = Bukkit.createInventory(player, Math.min(6, (int) Math.ceil(items.size() / 9)) * 9, player.getName());
					inventory.addItem(items.toArray(new ItemStack[items.size()]));
					player.openInventory(inventory);
					players.add(player.getUniqueId());
				}
			} else
				event.getPlayer().sendMessage(plugin.getMessage("notTime", "&cIt's not time yet! You need to wait until &l%date%").replaceFirst("%date%", dateFormat));
		} else if (plugin.admins.remove(player.getUniqueId())) {
			if (plugin.chests.remove(block.getLocation()))
				player.sendMessage(plugin.getMessage("unlinkChest", "&cChest successfully unlinked!"));
			else if (plugin.chests.add(block.getLocation()))
				player.sendMessage(plugin.getMessage("linkChest", "&cChest successfully linked!"));
		} else
			return;

		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void OnPlayerCloseInventory(InventoryCloseEvent event) {
		players.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler(ignoreCancelled = true)
	public void OnPlayerClickInventory(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (!players.contains(player) || event.getCurrentItem().getType() == Material.AIR)
			return;

		InventoryAction action = event.getAction();
		if (event.getClickedInventory().getType() == InventoryType.PLAYER && action == InventoryAction.MOVE_TO_OTHER_INVENTORY || (event.getClickedInventory().getType() == InventoryType.CHEST || event.getClickedInventory().getType() == InventoryType.ENDER_CHEST) && (event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_SOME || event.getAction() == InventoryAction.PLACE_ONE))
			event.setCancelled(true);
		else if (event.getClickedInventory().getType() != InventoryType.PLAYER && (action != InventoryAction.PICKUP_ALL || action != InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
			List<ItemStack> gifts = plugin.gifts.getOrDefault(player.getUniqueId(), new ArrayList<ItemStack>());
			if (gifts.remove(event.getCurrentItem()))
				plugin.gifts.put(player.getUniqueId().toString(), gifts);
		}
	}
}