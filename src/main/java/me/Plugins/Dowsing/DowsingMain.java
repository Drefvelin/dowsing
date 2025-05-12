package me.Plugins.Dowsing;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.Plugins.Dowsing.Loaders.BlockLoader;
import me.Plugins.Dowsing.Loaders.ConfigLoader;
import me.Plugins.Dowsing.Loaders.PMLoader;
import me.Plugins.Dowsing.Loaders.SlotLoader;
import me.Plugins.Dowsing.Loaders.TypeLoader;
import me.Plugins.Dowsing.Managers.CommandManager;
import me.Plugins.Dowsing.Managers.NodeManager;
import me.Plugins.Dowsing.Managers.ResourceManager;
import me.Plugins.Dowsing.Objects.Node;
import me.Plugins.Dowsing.Utils.Database;
import me.Plugins.Dowsing.Utils.TabCompletion;

public class DowsingMain extends JavaPlugin{
	FileConfiguration config = getConfig();
	
	public static DowsingMain plugin;
	
	
	ConfigLoader configLoader = new ConfigLoader();
	PMLoader pmLoader = new PMLoader();
	SlotLoader slotLoader = new SlotLoader();
	TypeLoader typeLoader = new TypeLoader();
	BlockLoader blockLoader = new BlockLoader();
	
	ResourceManager resourceManager = new ResourceManager();
	
	CommandManager commands = new CommandManager();
	NodeManager nodeManager = new NodeManager();
	
	Database db = new Database();
	
	
	public static Boolean isReloading = false;
	@Override
	public void onEnable(){
		plugin = this;
		createFolders();
		createConfigs();
		loadConfigs();
		startManagers();
		db.loadNodes();
		getServer().getPluginManager().registerEvents(resourceManager, plugin);
		getServer().getPluginManager().registerEvents(nodeManager, plugin);
		getCommand(commands.cmd1).setExecutor(commands);
		getCommand(commands.cmd1).setTabCompleter(new TabCompletion());
	}
	@Override
	public void onDisable() {
		db.deleteDatabase();
		for(Node n : NodeManager.nodes) {
			db.saveNode(n);
		}
	}
	public void startManagers() {
		nodeManager.start();
	}
	public void loadConfigs() {
		configLoader.loadConfig(new File(getDataFolder(), "config.yml"));
		pmLoader.loadConfig(new File(getDataFolder(), "production_methods.yml"));
		slotLoader.loadConfig(new File(getDataFolder(), "slots.yml"));
		typeLoader.loadConfig(new File(getDataFolder(), "types.yml"));
		blockLoader.loadConfig(new File(getDataFolder(), "blocks.yml"));
	}
	public void createFolders() {
		if (!getDataFolder().exists()) getDataFolder().mkdir();
		File subFolderR = new File(getDataFolder(), "Resources");
		if(!subFolderR.exists()) subFolderR.mkdir();
		File subFolderD = new File(getDataFolder(), "Nodes");
		if(!subFolderD.exists()) subFolderD.mkdir();
	}
	public void createConfigs() {
		String[] files = {
				"production_methods.yml",
				"slots.yml",
				"types.yml",
				"config.yml",
				"blocks.yml",
				};
		for(String s : files) {
			File newConfigFile = new File(getDataFolder(), s);
	        if (!newConfigFile.exists()) {
	        	newConfigFile.getParentFile().mkdirs();
	            saveResource(s, false);
	        }
		}
	}
	public void reloadConfigCommand() {
		nodeManager.cacheNodes();
		BlockLoader.clear();
		TypeLoader.clear();
		PMLoader.clear();
		SlotLoader.clear();
		loadConfigs();
		nodeManager.loadCache();
	}
	public void reloadConfigPCommand(Player p) {
		p.sendMessage(ChatColor.GREEN + "[Dowsing]" + ChatColor.YELLOW + " Reloading plugin...");
		reloadConfigCommand();
		p.sendMessage(ChatColor.GREEN + "[Dowsing]" + ChatColor.YELLOW + " Reloading complete!");
	}
}
