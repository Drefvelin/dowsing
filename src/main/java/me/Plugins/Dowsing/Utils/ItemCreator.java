package me.Plugins.Dowsing.Utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.lone.itemsadder.api.CustomStack;
import me.Plugins.Dowsing.Cache;
import me.Plugins.Dowsing.Objects.Level;
import me.Plugins.Dowsing.Objects.Node;
import me.Plugins.Dowsing.Objects.NodeType;
import me.Plugins.Dowsing.Objects.ProductionMethod;
import me.Plugins.SimpleFactions.Utils.Formatter;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.manager.ItemManager;

public class ItemCreator {
	@SuppressWarnings("deprecation")
	public ItemStack getItemFromPath(String s) {
		String type = s.split("\\.")[0]; //v.emerald
		ItemStack item = new ItemStack(Material.DIRT, 1);
		if(type.equalsIgnoreCase("v")) {
			item.setType(Material.valueOf(s.split("\\.")[1].toUpperCase()));
		} else if(type.equalsIgnoreCase("m")) {
			ItemManager itemManager = MMOItems.plugin.getItems();
			if(itemManager.getMMOItem(MMOItems.plugin.getTypes().get(s.split("\\.")[1].toUpperCase()), s.split("\\.")[2].toUpperCase()) == null){
				Bukkit.getLogger().info(s + " ia a malformed item input");
				return null;
			}
			item =  itemManager.getMMOItem(MMOItems.plugin.getTypes().get(s.split("\\.")[1].toUpperCase()), s.split("\\.")[2].toUpperCase()).newBuilder().build(); //m.material.salt
		} else {
			String itemPath = s.split("\\.")[1]; //ia.tfmc:abyssalite
			CustomStack stack = CustomStack.getInstance(itemPath);
			if(stack != null) {
				item = stack.getItemStack();
				item.setAmount(1);
			}
		}
		return item;
	}
	@SuppressWarnings("deprecation")
	public ItemStack createMenuItem(ConfigurationSection config, List<String> effects, List<String> cost, String prerequisite) {
		ItemStack i = new ItemStack(Material.DIRT, 1);
		String path = config.getString("material");
		if(path.split("\\.")[0].equalsIgnoreCase("v")) {
			i.setType(Material.valueOf(path.split("\\.")[1].toUpperCase()));
		} else if(path.split("\\.")[0].equalsIgnoreCase("ia")) {
			CustomStack stack = CustomStack.getInstance(path.split("\\.")[1]);
			if(stack != null) {
				i = stack.getItemStack();
			}
		} else if(path.split("\\.")[0].equalsIgnoreCase("m")) {
			ItemManager itemManager = MMOItems.plugin.getItems();
			if(itemManager.getMMOItem(MMOItems.plugin.getTypes().get(path.split("\\.")[1].toUpperCase()), path.split("\\.")[2].toUpperCase()) == null){
				Bukkit.getLogger().info(path + " is a malformed item input");
				return null;
			}
			i =  itemManager.getMMOItem(MMOItems.plugin.getTypes().get(path.split("\\.")[1].toUpperCase()), path.split("\\.")[2].toUpperCase()).newBuilder().build(); //m.material.salt
		}
		ItemMeta m = i.getItemMeta();
		if(config.contains("model_data")) {
			m.setCustomModelData(config.getInt("model_data"));
		}
		m.setDisplayName(config.getString("name"));
		List<String> lore = new ArrayList<String>();
		if(effects.size() != 0) {
			for(String s : effects) {
				lore.add(getFormattedEffect(s));
			}
		} else {
			lore.add("§7No Effects");
		}
		if(cost.size() != 0) {
			lore.add("§7Cost:");
			for(String s : cost) {
				lore.add(getFormattedCost(s, 1));
			}
		} else {
			lore.add("§7No Cost");
		}
		if(!prerequisite.equalsIgnoreCase("none")) {
			lore.add("§7Requires at least: §f"+WordUtils.capitalize(new String(prerequisite).replace("_", "")));
		}
		if(Cache.efficiencyLossPM > 0) {
			lore.add("");
			lore.add("§cEfficiency: §4-"+Cache.efficiencyLossPM+"%");
		}
		m.setLore(lore);
		i.setItemMeta(m);
		return i;
	}
	public ItemStack updateMenuItem(Node n, ProductionMethod pm) {
		ItemStack i = pm.getMenuItem();
		ItemMeta m = i.getItemMeta();
		List<String> lore = new ArrayList<String>();
		if(pm.getEffects().size() != 0) {
			for(String s : pm.getEffects()) {
				if(s.contains("extraction") && n.getNaturalYield() < 1) {
					continue;
				}
				lore.add(getFormattedEffect(s));
			}
		} else {
			lore.add("§7No Effects");
		}
		if(pm.getInputs().size() != 0) {
			lore.add("§7Cost:");
			for(String s : pm.getInputs()) {
				lore.add(getFormattedCost(s, n.getMultiplier()));
			}
		} else {
			lore.add("§7No Cost");
		}
		if(!pm.getPrerequisite().equalsIgnoreCase("none")) {
			lore.add("§7Requires at least: §f"+WordUtils.capitalize(new String(pm.getPrerequisite()).replace("_", " ")));
		}
		m.setLore(lore);
		i.setItemMeta(m);
		return i;
	}
	@SuppressWarnings("deprecation")
	public ItemStack createTypeItemConfig(ConfigurationSection config) {
		ItemStack i = new ItemStack(Material.DIRT, 1);
		String path = config.getString("material");
		if(path.split("\\.")[0].equalsIgnoreCase("v")) {
			i.setType(Material.valueOf(path.split("\\.")[1].toUpperCase()));
		} else if(path.split("\\.")[0].equalsIgnoreCase("ia")) {
			CustomStack stack = CustomStack.getInstance(path.split("\\.")[1]);
			if(stack != null) {
				i = stack.getItemStack();
			}
		} else if(path.split("\\.")[0].equalsIgnoreCase("m")) {
			ItemManager itemManager = MMOItems.plugin.getItems();
			if(itemManager.getMMOItem(MMOItems.plugin.getTypes().get(path.split("\\.")[1].toUpperCase()), path.split("\\.")[2].toUpperCase()) == null){
				Bukkit.getLogger().info(path + " is a malformed item input");
				return null;
			}
			i =  itemManager.getMMOItem(MMOItems.plugin.getTypes().get(path.split("\\.")[1].toUpperCase()), path.split("\\.")[2].toUpperCase()).newBuilder().build(); //m.material.salt
		}
		List<String> lore = new ArrayList<String>();
		ItemMeta m = i.getItemMeta();
		m.setLore(lore);
		i.setItemMeta(m);
		return i;
	}
	public ItemStack createTypeItemNode(Node n, NodeType t, boolean gui) {
		ItemStack i = t.getMenuItem();
		List<String> lore = new ArrayList<String>();
		ItemMeta m = i.getItemMeta();
		m.setDisplayName(t.getName());
		if(n.getModifiedTime() != t.getTimer()) {
			lore.add("§eTime: §f"+formatTime(n.getModifiedTime())+" §7(from "+formatTime(t.getTimer())+")");
		} else {
			lore.add("§eTime: §f"+formatTime(n.getModifiedTime()));
		}
		lore.add("§eYield: §a"+n.getYield());
		Formatter format = new Formatter();
		lore.add("§eTotal Upkeep: §f"+format.formatDouble(n.getUpkeep()*n.getCostIncrease())+"d");
		lore.add("§7Possible Drops:");
		Double maxWeight = 0.0;
		for(String s : n.getCompleteDrop().keySet()) {
			Double amount = n.getCompleteDrop().get(s);
			maxWeight = maxWeight+amount;
		}
		for(String s : n.getCompleteDrop().keySet()) {
			lore.add("§f"+getFormattedDrop(s, n.getCompleteDrop().get(s), maxWeight));
		}
		if(gui & Cache.efficiencyLossType > 0) {
			lore.add("");
			lore.add("§cEfficiency: §4-"+Cache.efficiencyLossType+"%");
		}
		m.setLore(lore);
		i.setItemMeta(m);
		return i;
	}
	public ItemStack createNaturalYieldItem(Node n) {
		ItemStack i = new ItemStack(Material.EMERALD, 1);
		ItemMeta m = i.getItemMeta();
		m.setDisplayName("§aChunk has a natural yield of §e"+n.getNaturalYield()+"§a for §e"+WordUtils.capitalize(n.getCurrentType().getResource()));
		List<String> lore = new ArrayList<String>();
		lore.add("§7Bonuses will be applied depending on Extraction level");
		lore.add("§eCurrent Extraction: §f"+n.getExtraction()+" §7(max "+n.getNaturalYield()+")");
		lore.add("§eTime Modifier per Extraction: §a"+n.getCurrentType().getTimeNaturalYield()+"%");
		lore.add("§a"+n.getCurrentType().getYieldNaturalYield()+" §eExtraction per §a1 §eYield");
		m.setLore(lore);
		i.setItemMeta(m);
		return i;
	}
	public List<String> getUpgradeDownGradeFormatted(Node n, String type) {
		List<String> list = new ArrayList<String>();
		Level cLvl = n.getCurrentType().getLevels().get(n.getLevel()-1);
		Level nLvl = null;
		if(type.equalsIgnoreCase("upgrade") && n.getLevel() < n.getCurrentType().getLevels().size()) {
			nLvl = n.getCurrentType().getLevels().get(n.getLevel());
		} else if(type.equalsIgnoreCase("downgrade") && n.getLevel() > 1) {
			nLvl = n.getCurrentType().getLevels().get(n.getLevel()-2);
		} else {
			nLvl = cLvl;
		}
		Integer oldYield = 0;
		Integer oldPrestige = 0;
		Integer oldExtraction = 0;
		Double oldTime = 0.0;
		Double oldUpkeep = 0.0;
		for(String s : cLvl.getEffects()) {
			String t = s.split("\\(")[0];
			if(t.equalsIgnoreCase("yield")) {
				oldYield = Integer.parseInt(s.split("\\(")[1].replace(")", ""));
			} else if(t.equalsIgnoreCase("time_modifier")) {
				oldTime = Double.parseDouble(s.split("\\(")[1].replace(")", ""));
			} else if(t.equalsIgnoreCase("prestige")) {
				oldPrestige = Integer.parseInt(s.split("\\(")[1].replace(")", ""));
			} else if(t.equalsIgnoreCase("extraction")) {
				oldExtraction = Integer.parseInt(s.split("\\(")[1].replace(")", ""));
			} else if(t.equalsIgnoreCase("upkeep")) {
				oldUpkeep = Double.parseDouble(s.split("\\(")[1].replace(")", ""));
			}
		}
		Integer newYield = 0;
		Integer newPrestige = 0;
		Integer newExtraction = 0;
		Double newTime = 0.0;
		Double newUpkeep = 0.0;
		for(String s : nLvl.getEffects()) {
			String t = s.split("\\(")[0];
			if(t.equalsIgnoreCase("yield")) {
				newYield = Integer.parseInt(s.split("\\(")[1].replace(")", ""));
			}
			if(t.equalsIgnoreCase("time_modifier")) {
				newTime = Double.parseDouble(s.split("\\(")[1].replace(")", ""));
			} else if(t.equalsIgnoreCase("prestige")) {
				newPrestige = Integer.parseInt(s.split("\\(")[1].replace(")", ""));
			} else if(t.equalsIgnoreCase("extraction")) {
				newExtraction = Integer.parseInt(s.split("\\(")[1].replace(")", ""));
			} else if(t.equalsIgnoreCase("upkeep")) {
				newUpkeep = Double.parseDouble(s.split("\\(")[1].replace(")", ""));
			}
		}
		if(newPrestige > 0 || oldPrestige > 0) {
			list.add(oldNewInteger(oldPrestige, newPrestige, "§9Prestige", false));
		}
		list.add(oldNewInteger(oldYield, newYield, "§eYield", false));
		if((newExtraction > 0 || oldExtraction > 0) && n.getNaturalYield() > 0) {
			list.add(oldNewInteger(oldExtraction, newExtraction, "§eExtraction", false));
		}
		if(newUpkeep > 0 || oldUpkeep > 0) {
			list.add(oldNewUpkeep(oldUpkeep, newUpkeep, "§eUpkeep", true));
		}
		if(Double.compare(newTime, oldTime) == 0) {
			list.add("§eTime Modifier: §f"+formatTimeModifier(oldTime)+"%");
		} else if(newTime > oldTime) {
			list.add("§eTime Modifier: §c"+formatTimeModifier(oldTime)+"%->"+formatTimeModifier(newTime)+"%");
		} else {
			list.add("§eTime Modifier: §a"+formatTimeModifier(oldTime)+"%->"+formatTimeModifier(newTime)+"%");
		}
		return list;
	}
	public String oldNewInteger(Integer old, Integer n, String t, Boolean reverse) {
		String s = "";
		if(n == old) {
			s = t+": §f"+old;
		} else if(n > old) {
			if(reverse) {
				s = t+": §c"+old+"->"+n;
			} else {
				s = t+": §a"+old+"->"+n;
			}
		} else {
			if(reverse) {
				s = t+": §a"+old+"->"+n;
			} else {
				s = t+": §c"+old+"->"+n;
			}
		}
		return s;
	}
	public String oldNewUpkeep(Double old, Double n, String t, Boolean reverse) {
		String s = "";
		if(Double.compare(n, old) == 0) {
			s = t+": §f"+old;
		} else if(n > old) {
			if(reverse) {
				s = t+": §c"+old+"d->"+n+"d";
			} else {
				s = t+": §a"+old+"d->"+n+"d";
			}
		} else {
			if(reverse) {
				s = t+": §a"+old+"d->"+n+"d";
			} else {
				s = t+": §c"+old+"d->"+n+"d";
			}
		}
		return s;
	}
	String formatTimeModifier(Double m) {
		String s = String.valueOf(m);
		if(m > 0) {
			s = "+"+m;
		}
		return s;
	}
	public String formatTime(Integer time) {
		Integer remainder = time % 60;
		Integer hoursTime = time/60;
		Integer minutes = remainder % 60;
		String hours = String.valueOf(hoursTime);
		String mins = String.valueOf(minutes);
		String formattedTime = "";
		if(hoursTime > 0) {
			formattedTime = formattedTime+hours + "h ";
		}
		if(minutes > 0) {
			formattedTime = formattedTime + mins + "m ";
		}
		if(minutes == 0 && hoursTime == 0) {
			formattedTime = "0m";
		}
		return formattedTime;
	}
	public String getFormattedDrop(String item, Double amount, Double maxWeight) {
		Double chance = amount/maxWeight;
		chance = chance*100;
		Formatter format = new Formatter();
		chance = format.formatDouble(chance);
		String name = "";
		if(item.equalsIgnoreCase("Nothing")) {
			name = "Nothing";
		} else {
			name = getItemName(item);
		}
		return "§f"+name+"§f "+chance+"%";
	}
	public String getFormattedEffect(String s){
		String type = s.split("\\(")[0];
		String effect = s.split("\\(")[1].replace(")", "");
		if(type.equalsIgnoreCase("time_modifier")) {
			Double amount = Double.parseDouble(effect);
			s = "§eTime Modifier: ";
			if(amount > 0) {
				s = s+"§c+"+amount+"%";
			} else {
				s = s+"§a"+amount+"%";
			}
		} else if(type.equalsIgnoreCase("yield")) {
			Integer amount = Integer.parseInt(effect);
			s = "§eYield: ";
			if(amount > 0) {
				s = s+"§a+"+amount;
			} else {
				s = s+"§c"+amount;
			}
		} else if(type.equalsIgnoreCase("add_drop")) {
			String item = effect.split("\\,")[0];
			Double amount = Double.parseDouble(effect.split("\\,")[1]);
			Formatter format = new Formatter();
			amount = format.formatDouble(amount);
			String name = "";
			if(item.equalsIgnoreCase("Nothing")) {
				name = "Nothing";
			} else {
				name = getItemName(item);
			}
			s = "§eAdded Drop: §f"+name+" §7(Weight: "+amount+")";
		} else if(type.equalsIgnoreCase("prestige")) {
			Integer amount = Integer.parseInt(effect);
			s = "§9Prestige: ";
			if(amount > 0) {
				s = s+"§f+"+amount;
			} else {
				s = s+"§c"+amount;
			}
		} else if(type.equalsIgnoreCase("extraction")) {
			Integer amount = Integer.parseInt(effect);
			s = "§eExtraction: ";
			if(amount > 0) {
				s = s+"§f+"+amount;
			} else {
				s = s+"§c"+amount;
			}
		} else if(type.equalsIgnoreCase("upkeep")) {
			Double amount = Double.parseDouble(effect);
			Formatter format = new Formatter();
			amount = format.formatDouble(amount);
			s = "§eUpkeep: "+"§f"+amount+"d";
		}
		return s;
	}
	public String getFormattedCost(String s, Integer m){
		String item = s.split("\\(")[0];
		Integer amount = Integer.parseInt(s.split("\\(")[1].replace(")", ""));
		return "§f"+getItemName(item)+"§f x"+(amount*m);
	}
	@SuppressWarnings("deprecation")
	String getItemName(String path) {
		String s = "";
		String type = path.split("\\.")[0];
		if(type.equalsIgnoreCase("v")) {
			s = WordUtils.capitalize(path.split("\\.")[1].replace("_", " "));
		} else if(path.split("\\.")[0].equalsIgnoreCase("ia")) {
			CustomStack stack = CustomStack.getInstance(path.split("\\.")[1]);
			if(stack != null) {
				ItemStack i = stack.getItemStack();
				s = i.getItemMeta().getDisplayName();
			}
		} else if(path.split("\\.")[0].equalsIgnoreCase("m")) {
			ItemManager itemManager = MMOItems.plugin.getItems();
			ItemStack item =  itemManager.getMMOItem(MMOItems.plugin.getTypes().get(path.split("\\.")[1].toUpperCase()), path.split("\\.")[2].toUpperCase()).newBuilder().build(); //m.material.salt
			s = item.getItemMeta().getDisplayName();
		}
		return s;
	}
}
