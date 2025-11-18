package me.Plugins.Dowsing.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import me.Plugins.Dowsing.Cache;
import me.Plugins.Dowsing.DowsingMain;
import me.Plugins.Dowsing.Loaders.BlockLoader;
import me.Plugins.Dowsing.Loaders.PMLoader;
import me.Plugins.Dowsing.Loaders.TypeLoader;
import me.Plugins.Dowsing.Objects.Level;
import me.Plugins.Dowsing.Objects.Node;
import me.Plugins.Dowsing.Objects.NodeBlock;
import me.Plugins.Dowsing.Objects.NodeSlot;
import me.Plugins.Dowsing.Objects.NodeType;
import me.Plugins.Dowsing.Objects.ProductionMethod;
import me.Plugins.Dowsing.Utils.ItemDropper;
import me.Plugins.Dowsing.Utils.NodeEngine;
import me.Plugins.Dowsing.Utils.NodeReloader;
import me.Plugins.Dowsing.enums.ConfirmType;
import me.Plugins.SimpleFactions.Events.FactionDeleteEvent;
import me.Plugins.SimpleFactions.Managers.FactionManager;
import me.Plugins.SimpleFactions.Objects.Faction;
import me.Plugins.SimpleFactions.Utils.Permissions;

public class NodeManager implements Listener{
	public static List<Node> nodes = new ArrayList<Node>();
	public HashMap<Player, Node> currentNode = new HashMap<>();
	public HashMap<Player, NodeSlot> currentSlot = new HashMap<>();
	public HashMap<Player, NodeType> currentType = new HashMap<>();
	public HashMap<Player, ConfirmType> confirm = new HashMap<>();
	public HashMap<Location, NodeReloader> cached = new HashMap<>();
	public static Integer getNodeAmount(Faction f) {
		Integer i = 0;
		for(Node n : nodes) {
			if(!n.hasFaction()) continue;
			if(n.getBlock().isSpecial()) continue;
			if(n.getFaction().getId().equalsIgnoreCase(f.getId())) i++;
		}
		return i;
	}
	public static Integer getNodeCapacity(Faction f) {
		int capacity = 1;
		if(Cache.extraCapacity) {
			int members = f.getMembers().size();
			int added = (int) Math.floorDiv(members, Cache.membersPerCapacity);
			if(added > Cache.maxMemberCapacity) {
				added = Cache.maxMemberCapacity;
			}
			capacity = capacity+added+f.getExtraNodeCapacity();
		}
		return capacity;
	}
	public Node getByLocation(Location loc) {
		for(Node n : nodes) {
			if(n.getLoc().equals(loc)) return n;
		}
		return null;
	}
	public String getClickedFurniture(Block b) {
		List<Entity> nearbyEntities = (List<Entity>) b.getWorld().getNearbyEntities(b.getLocation(), 0.2, 0.2, 0.2);
		for(Entity a : b.getWorld().getEntities()){
            if(nearbyEntities.contains(a)){
            	CustomFurniture f = CustomFurniture.byAlreadySpawned(a);
                if(f != null) {
                	return f.getNamespace();
                }
            }
        }
		return "none";
}
	public void start() {
		particleCycle();
		hourCycle();
		new BukkitRunnable()
		{
			public void run()
			   {
					validate();
					for(Node n : nodes) {
						if(!n.getIsActive()) continue;
						if(!n.hasFaction()) continue;
						if(n.getLoc().getChunk().isForceLoaded() == false) {
							n.getLoc().getChunk().setForceLoaded(true);
						}
						n.tickCycle();
						if(n.getCycleTime().equals(Cache.cycleLength)) {
							n.setCycleTime(0);
							n.input();
						}
						if(n.getTimeLeft() > 0) {
							n.tick();
						} else {
							ItemDropper dropper = new ItemDropper();
							dropper.dropItems(n);
							n.setTimeLeft(n.getModifiedTime());
							n.setInputCounter(0);
						}
						for(Player p : Bukkit.getOnlinePlayers()) {
							if(currentNode.containsKey(p)) {
								if(currentNode.get(p).getId().equals(n.getId())) {
									InventoryManager inv = new InventoryManager();
									Inventory i = p.getOpenInventory().getTopInventory();
									if(i == null) continue;
									if(p.getOpenInventory() == null) continue;
									if(p.getOpenInventory().getTitle().equalsIgnoreCase("§7"+n.getBlock().getResource()+ " Node")) {
										inv.updateNodeView(p, n, i);
									}
								}
							}
						}
					}	
			   }
		}.runTaskTimer(DowsingMain.plugin, 0L, 1200L);
	}
	public void particleCycle(){
		new BukkitRunnable()
		{
			public void run()
			{
				for(Node n : nodes){
					if(n.isClaimable()){
						Location loc = n.getLoc().clone().add(0.5, 1, 0.5);
						loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 10);
					} else {
						n.check();
					}
				}	
			}
		}.runTaskTimer(DowsingMain.plugin, 0L, 5L);
	}
	public void hourCycle(){
		new BukkitRunnable()
		{
			public void run()
			{
				for(Node n : nodes){
					n.growEfficiency();
				}	
			}
		}.runTaskTimer(DowsingMain.plugin, 0L, 72000L);
	}
	public void validate() {
		for(int i = 0; i<nodes.size();i++) {
			Node n = nodes.get(i);
			if(!n.getLoc().getBlock().getType().equals(Material.BARRIER)) {
				n.breakNode();
				nodes.remove(i);
			}
		}
	}
	public void confirmClick(Player p, Node n, ConfirmType t) {
		if(t.equals(ConfirmType.DEACTIVATE)) {
			n.deActivate();
			InventoryManager inv = new InventoryManager();
			inv.nodeView(p, n);
			currentNode.put(p, n);
		}
		if(t.equals(ConfirmType.DELETE_NODE)) {
			n.breakNode();
			nodes.remove(n);
			p.closeInventory();
		}
		if(t.equals(ConfirmType.CHANGE_TYPE)) {
			NodeType nt = currentType.get(p);
			n.setCurrentType(nt);
			//n.setLevel(1);
			n.updateEfficiency(-Cache.efficiencyLossType);
			n.update();
			InventoryManager inv = new InventoryManager();
			inv.nodeView(p, n);
			currentNode.put(p, n);
		}
	}
	@EventHandler
	public void placeNode(BlockPlaceEvent e) {
		if(getByLocation(e.getBlock().getLocation()) != null) return;
		Player p = e.getPlayer();
		String type = "none";
		ItemStack i = p.getInventory().getItemInMainHand();
		CustomStack fur = CustomStack.byItemStack(i);
		if(fur != null) {
			if(BlockLoader.getByPath(fur.getNamespacedID()) != null) type = "furniture";
		} else if(BlockLoader.getByBlock(e.getBlock().getType()) != null) type = "block";
		NodeBlock b = null;
		if(type.equalsIgnoreCase("furniture")) {
			b = BlockLoader.getByPath(fur.getNamespacedID());
		} else if(type.equalsIgnoreCase("block")) {
			b = BlockLoader.getByBlock(e.getBlock().getType());
		}
		if(b == null) return;
		for(Node node : nodes) {
			if(node.getLoc().getChunk().equals(e.getBlock().getChunk())) {
				p.sendMessage("§cChunk already has a node!");
				e.setCancelled(true);
				return;
			}
		}
		Faction f = FactionManager.getByMember(p.getName());
		if(f == null) {
			p.sendMessage("§cYou need to have a faction to use nodes!");
			e.setCancelled(true);
			return;
		}
		if(f.getMembers().size() < Cache.minMembersForNode && !b.isSpecial()){
			p.sendMessage("§cYou need at least "+Cache.minMembersForNode+" members in your faction to have a node!");
			e.setCancelled(true);
			return;
		}
		if(getNodeAmount(f)-getNodeCapacity(f) >= 0 && !b.isSpecial()) {
			p.sendMessage("§cYou are already filled your node capacity!");
			e.setCancelled(true);
			return;
		}
		Node n = new Node(e.getBlock().getLocation(), f, b);
		p.sendMessage("Node created");
		p.getLocation().getWorld().playSound(n.getLoc(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
		nodes.add(n);
		n.activate();
		n.update();
	}
	@EventHandler
	public void breakFurnitureNode(FurnitureBreakEvent e) {
		if(BlockLoader.getByPath(e.getFurniture().getNamespacedID()) == null) return;
		for(Node n : nodes) {
			if(n.getLoc().getWorld().equals(e.getFurniture().getArmorstand().getLocation().getWorld())) {
				if(n.getLoc().distance(e.getFurniture().getArmorstand().getLocation()) < 1.5) {
					e.setCancelled(true);
				}
			}
		}
	}
	@EventHandler
	public void breakNode(BlockBreakEvent e) {
		if(BlockLoader.getByBlock(e.getBlock().getType()) == null) return;
		for(Node n : nodes) {
			if(n.getLoc().equals(e.getBlock().getLocation())) {
				e.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void deleteFaction(FactionDeleteEvent e) {
		for(int i = 0; i<nodes.size(); i++) {
			Node n = nodes.get(i);
			if(n.getFaction().getId().equalsIgnoreCase(e.getFaction().getId())) {
				n.setFaction(null);;
			}
		}
	}
	@EventHandler
	public void openNode(PlayerInteractEvent e) {
		if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
		Player p = e.getPlayer();
		if(getByLocation(e.getClickedBlock().getLocation()) == null) return;
		Node n = getByLocation(e.getClickedBlock().getLocation());
		InventoryManager inv = new InventoryManager();
		e.setCancelled(true);
		if(n.isClaimable()){
			Faction f = FactionManager.getByLeader(p.getName());
			if(f == null) {
				p.sendMessage("§cMust be a faction leader to claim an unclaimed node!");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
				return;
			}
			if(!n.canClaim(p, f)) return;
			p.sendMessage("§aClaimed Node");
			n.setFaction(FactionManager.getByLeader(p.getName()));
			p.getLocation().getWorld().playSound(n.getLoc(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
			n.update();
			inv.nodeView(p, n);
			currentNode.put(p, n);
			return;
		}
		if(!n.hasFaction()) {
			p.sendMessage("§cNode had no faction and so it broke");
			n.breakNode();
			nodes.remove(n);
			return;
		}
		n.update();
		inv.nodeView(p, n);
		currentNode.put(p, n);
	}
	@EventHandler
	public void invenClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if(!currentNode.containsKey(p)) return;
		Node n = currentNode.get(p);
		InventoryManager inv = new InventoryManager();
		if(e.getView().getTitle().equalsIgnoreCase("§7"+n.getBlock().getResource()+ " Node")) {
			e.setCancelled(true);
			if(!n.hasFaction()) {
				n.breakNode();
				nodes.remove(n);
				p.closeInventory();
				return;
			}
			Faction f = FactionManager.getByMember(p.getName());
			if(!p.hasPermission("dowsing.admin") && (f == null || !n.getFaction().getId().equalsIgnoreCase(f.getId()))) {
				p.sendMessage("§cCannot change another faction's node");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
				return;
			}
			if(e.getSlot() == 8) {
				if(n.getIsActive()) {
					p.sendMessage("§cCannot upgrade while node is active");
					p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
					return;
				}
				upgradeNode(p, n, e.getClickedInventory());
			} else if(e.getSlot() == 9) {
				if(n.getIsActive()) {
					p.sendMessage("§cCannot change type while node is active");
					p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
					return;
				}
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
				inv.typeView(p, n);
			} else if(e.getSlot() == 24) {
				if(f.canPurchaseCapacity()) {
					purchaseCapacity(p, f, n, e.getClickedInventory());
				} else if(e.getCurrentItem().getType().equals(Material.NETHER_STAR)) {
					p.sendMessage("§cAlready purchased the maximum extra capacity");
					p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
					return;
				}
			} else if(e.getSlot() == 26) {
				if(n.getIsActive()) {
					p.sendMessage("§cCannot downgrade while node is active");
					p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
					return;
				}
				downgradeNode(p, n, e.getClickedInventory());
			} else if(e.getSlot() == 17) {
				if(n.getIsActive()) {
					confirm.put(p, ConfirmType.DEACTIVATE);
					inv.confirmView(p);
				} else {
					n.activate();
					if(n.getIsActive()) {
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
					} else {
						p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
					}
				}
				n.update();
				inv.updateNodeView(p, n, e.getClickedInventory());
			} else if(e.getSlot() == 18) {
				if(!(n.getBlock().isBreakable() || Permissions.isAdmin(p))) return;
				if(n.getIsActive()) {
					p.sendMessage("§cCannot delete node while active");
					p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
					return;
				}
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
				confirm.put(p, ConfirmType.DELETE_NODE);
				inv.confirmView(p);
			} else if(e.getSlot() == 6) {
				if(!n.getBlock().isTransferable()) return;
				n.setFaction(null);
				p.sendMessage("§aNode set as claimable");
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
				p.closeInventory();
			} else{
				for(NodeSlot slot : n.getCurrentType().getSlots()) {
					if(slot.getSlot().equals(e.getSlot())) {
						if(n.getIsActive()) {
							p.sendMessage("§cCannot change production methods while node is active");
							p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
							return;
						}
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
						inv.slotView(p, n, slot);
						currentSlot.put(p, slot);
					}
				}
			}
		} else if(currentSlot.get(p) != null && e.getView().getTitle().equalsIgnoreCase("§7"+n.getBlock().getResource()+" Node: "+WordUtils.capitalize(currentSlot.get(p).getId().replace("_", " ")))) {
			e.setCancelled(true);
			if(!n.hasFaction()) {
				n.breakNode();
				nodes.remove(n);
				p.closeInventory();
				return;
			}
			if(e.getSlot() == 26) {
				inv.nodeView(p, n);
				currentNode.put(p, n);
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
				return;
			}
			NodeSlot slot = currentSlot.get(p);
			ItemStack i = e.getCurrentItem();
			if(i == null) return;
			ProductionMethod pm = PMLoader.getByItemName(i.getItemMeta().getDisplayName());
			if(pm == null) return;
			if(pm.getId().equalsIgnoreCase(slot.getActivePm().getId())) return;
			if(!pm.getPrerequisite().equalsIgnoreCase("none")){
				NodeEngine ng = new NodeEngine();
				if(!ng.checkPrerequisite(pm, n)) {
					p.sendMessage("§cThis production method requires at least "+WordUtils.capitalize(pm.getPrerequisite().replace("_", " ")));
					p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
					return;
				}
			}
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
			slot.setActivePm(pm);
			n.updateEfficiency(-Cache.efficiencyLossPM);
			n.update();
			inv.nodeView(p, n);
			currentNode.put(p, n);
		} else if(e.getView().getTitle().equalsIgnoreCase("§7"+n.getBlock().getResource()+" Node: Type")) {
			e.setCancelled(true);
			if(!n.hasFaction()) {
				n.breakNode();
				nodes.remove(n);
				p.closeInventory();
				return;
			}
			if(e.getSlot() == 26) {
				inv.nodeView(p, n);
				currentNode.put(p, n);
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
				return;
			}
			ItemStack i = e.getCurrentItem();
			if(i == null) return;
			NodeType t = TypeLoader.getByItemName(i.getItemMeta().getDisplayName());
			if(t == null) return;
			if(t.getId().equalsIgnoreCase(n.getCurrentType().getId())) return;
			if(t.getBiomes().size() > 0) {
				String biome = n.getLoc().getBlock().getBiome().toString();
				if(!t.getBiomes().contains(biome)) {
					p.sendMessage("§cThis node type can only be used in these biomes:");
					for(String s : t.getBiomes()) {
						p.sendMessage("§f- "+WordUtils.capitalize(s.replace("_", " ")));
					}
					p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
					return;
				}
			}
			confirm.put(p, ConfirmType.CHANGE_TYPE);
			currentType.put(p, t);
			inv.confirmView(p);
		} else if(e.getView().getTitle().equalsIgnoreCase("§7Confirm Action")) {
			e.setCancelled(true);
			if(!n.hasFaction()) {
				n.breakNode();
				nodes.remove(n);
				p.closeInventory();
				return;
			}
			if(!confirm.containsKey(p)) return;
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
			if(e.getSlot() == 11) {
				confirmClick(p, n, confirm.get(p));
				confirm.remove(p);
			} else if(e.getSlot() == 15) {
				inv.nodeView(p, n);
				currentNode.put(p, n);
			}
		}
	}
	private void purchaseCapacity(Player p, Faction f, Node n, Inventory i) {
		double cost = n.getNodeCapacityCost();
		if(f.getBank() == null) {
			p.sendMessage("§cNo bank");
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
			return;
		}
		if(f.getBank().getWealth() < cost) {
			p.sendMessage("§cNot enough funds");
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
			return;
		}
		f.getBank().withdraw(cost);
		f.setExtraNodeCapacity(f.getExtraNodeCapacity()+1);
		p.sendMessage("§aPurchased +1 Capacity");
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
		InventoryManager inv = new InventoryManager();
		inv.updateNodeView(p, n, i);
		
	}
	public void upgradeNode(Player p, Node n, Inventory i) {
		if(n.getLevel() >= n.getCurrentType().getLevels().size()) {
			p.sendMessage("§cNode is already at max level");
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
			return;
		}
		Level newLvl = n.getCurrentType().getLevels().get(n.getLevel());
		Faction f = n.getFaction();
		Double cost = newLvl.getCost()*n.getCostIncrease();
		if(f.getBank() == null || f.getBank().getWealth() < cost) {
			p.sendMessage("§cFaction bank does not have enough funds");
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
			return;
		}
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
		f.getBank().withdraw(cost);
		n.setLevel(n.getLevel()+1);
		InventoryManager inv = new InventoryManager();
		n.update();
		inv.updateNodeView(p, n, i);
	}
	public void downgradeNode(Player p, Node n, Inventory i) {
		if(n.getLevel() == 1) {
			p.sendMessage("§cNode cannot go below level 1");
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
			return;
		}
		Level lvl = n.getCurrentType().getLevels().get(n.getLevel()-1);
		Double refund = lvl.getCost()*Cache.refundPercentage;
		n.getFaction().getBank().deposit(refund);
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
		n.setLevel(n.getLevel()-1);
		InventoryManager inv = new InventoryManager();
		n.update();
		inv.updateNodeView(p, n, i);
	}
	public void cacheNodes() {
		cached.clear();
		for(Node n : nodes) {
			NodeReloader reloader = new NodeReloader();
			reloader.cache(n);
			cached.put(n.getLoc(), reloader);
		}
	}
	public void loadCache() {
		for(Node n : nodes) {
			n.setBlock(BlockLoader.getByString(n.getBlock().getId()));
			n.setCurrentType(TypeLoader.getByString(n.getCurrentType().getId()));
			NodeReloader reloader = cached.get(n.getLoc());
			reloader.reload(n);
			n.update();
		}
	}
}
