package me.Plugins.Dowsing.Utils;

import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.lib.api.item.NBTItem;
import me.Plugins.Dowsing.Objects.Node;
import me.Plugins.Dowsing.Objects.NodeSlot;
import me.Plugins.Dowsing.Objects.ProductionMethod;

public class NodeEngine {
	public Boolean hasBarrel(Node n) {
		Location l = new Location(n.getLoc().getWorld(), n.getLoc().getX(), n.getLoc().getY(), n.getLoc().getZ());
		l.add(0,-1,0);
		Block b = l.getBlock();
		if(b.getType().equals(Material.BARREL)) return true;
		return false;
	}
	public Boolean hasHopper(Node n) {
		Location l = new Location(n.getLoc().getWorld(), n.getLoc().getX(), n.getLoc().getY(), n.getLoc().getZ());
		l.add(0,1,0);
		Block b = l.getBlock();
		if(b.getType().equals(Material.HOPPER)) return true;
		return false;
	}
	public Boolean hasInputs(Node n) {
		Location l = new Location(n.getLoc().getWorld(), n.getLoc().getX(), n.getLoc().getY(), n.getLoc().getZ());
		l.add(0,-1,0);
		Block block = l.getBlock();
		Barrel b = (Barrel) block.getState();
		Inventory i = b.getInventory();
		HashMap<String, Integer> inputs = new HashMap<>();
		for(NodeSlot slot : n.getCurrentType().getSlots()) {
			for(String s : slot.getActivePm().getInputs()) {
				String key = s.split("\\(")[0];
				Integer amount = Integer.parseInt(s.split("\\(")[1].replace(")", ""));
				amount = amount*n.getMultiplier();
				if(inputs.containsKey(key)) {
					amount = amount +inputs.get(key);
				}
				inputs.put(key, amount);
			}
		}
		for(String key : inputs.keySet()) {
			if(!check(n, key, inputs.get(key), i)) return false;
		}
		return true;
	}
	Boolean check(Node n, String path, Integer amount, Inventory i) {
		for(ItemStack item : i.getContents()) {
			if(item != null && compareItem(path, item)) {
				if(item.getAmount() < amount) {
					amount = amount-item.getAmount();
				} else {
					return true;
				}
			}
		}
		return false;
	}
	Boolean compareItem(String path, ItemStack item) {
		String type = path.split("\\.")[0];
		if(type.equalsIgnoreCase("v")) {
			if(CustomStack.byItemStack(item) != null) return false;
			if(NBTItem.get(item).hasType()) return false;
			if(item.getType().equals(Material.valueOf(path.split("\\.")[1].toUpperCase()))) return true;
		} else if(path.split("\\.")[0].equalsIgnoreCase("ia")) {
			if(CustomStack.byItemStack(item) == null) return false;
			CustomStack stack = CustomStack.byItemStack(item);
			if(stack.getConfigPath().equalsIgnoreCase(path.split("\\.")[1])) return true;
		} else if(path.split("\\.")[0].equalsIgnoreCase("m")) {
			NBTItem nbt = NBTItem.get(item);
			if(!nbt.hasType()) return false;
			if(nbt.getType().equalsIgnoreCase(path.split("\\.")[1]) && nbt.getString("MMOITEMS_ITEM_ID").equalsIgnoreCase(path.split("\\.")[2])) return true;
		}
		return false;
	}
	public void refund(Node n) {
		Location l = new Location(n.getLoc().getWorld(), n.getLoc().getX(), n.getLoc().getY(), n.getLoc().getZ());
		l.add(0,-1,0);
		Block block = l.getBlock();
		Barrel b = (Barrel) block.getState();
		Inventory i = b.getInventory();
		HashMap<String, Integer> inputs = new HashMap<>();
		for(NodeSlot slot : n.getCurrentType().getSlots()) {
			for(String s : slot.getActivePm().getInputs()) {
				String key = s.split("\\(")[0];
				Integer amount = Integer.parseInt(s.split("\\(")[1].replace(")", ""));
				amount = amount*n.getMultiplier();
				if(inputs.containsKey(key)) {
					amount = amount +inputs.get(key);
				}
				inputs.put(key, amount);
			}
		}
		if(n.getFaction().getBank() != null) {
			n.getFaction().getBank().deposit(n.getUpkeep());
		}
		n.setInputCounter(n.getInputCounter()-1);
		for(String key : inputs.keySet()) {
			addItem(key, inputs.get(key), i);
		}
	}
	public void addItem(String path, Integer amount, Inventory i) {
		ItemCreator ic = new ItemCreator();
		ItemStack item = ic.getItemFromPath(path);
		item.setAmount(amount);
		i.addItem(item);
	}
	public void takeInputs(Node n) {
		Location l = new Location(n.getLoc().getWorld(), n.getLoc().getX(), n.getLoc().getY(), n.getLoc().getZ());
		l.add(0,-1,0);
		Block block = l.getBlock();
		Barrel b = (Barrel) block.getState();
		Inventory i = b.getInventory();
		HashMap<String, Integer> inputs = new HashMap<>();
		for(NodeSlot slot : n.getCurrentType().getSlots()) {
			for(String s : slot.getActivePm().getInputs()) {
				String key = s.split("\\(")[0];
				Integer amount = Integer.parseInt(s.split("\\(")[1].replace(")", ""));
				amount = amount*n.getMultiplier();
				if(inputs.containsKey(key)) {
					amount = amount +inputs.get(key);
				}
				inputs.put(key, amount);
			}
		}
		n.setInputCounter(n.getInputCounter()+1);
		for(String key : inputs.keySet()) {
			take(n, key, inputs.get(key), i);
		}
	}
	public void take(Node n, String path, Integer amount, Inventory i) {
		for(ItemStack item : i.getContents()) {
			if(item != null && compareItem(path, item)) {
				if(item.getAmount() < amount) {
					amount = amount-item.getAmount();
					item.setAmount(0);
				} else {
					item.setAmount(item.getAmount()-amount);
					return;
				}
			}
		}
	}
	public boolean checkPrerequisite(ProductionMethod pm, Node n) {
		if(pm.getPrerequisite().equalsIgnoreCase("none")) return true;
		for(NodeSlot slot : n.getCurrentType().getSlots()) {
			for(ProductionMethod slotpm : slot.getPms()) {
				if(slotpm.getId().equalsIgnoreCase(pm.getPrerequisite())) {
					int needed = slotpm.getWeight();
					int current = slot.getActivePm().getWeight();
					if(needed <= current) return true;
				}
			}
		}
		return false;
	}
}
