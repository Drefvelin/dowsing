package me.Plugins.Dowsing.Loaders;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.Plugins.Dowsing.Cache;


public class ConfigLoader {
	
	public void loadConfig(File configFile) {
		FileConfiguration config = new YamlConfiguration();
        try {
        	config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
		Cache.dowsingStick = config.getString("dowsing_item");
		Cache.cycleLength = config.getInt("input_cycle_length");
		Cache.naturalYieldEnabled = config.getBoolean("enable-natural-yields");
		if(config.getInt("members_per_node_capacity") != -1) {
			if(config.getInt("members_per_node_capacity") < 1) {
				Cache.membersPerCapacity = 1;
			} else {
				Cache.membersPerCapacity = config.getInt("members_per_node_capacity");
			}
			Cache.extraCapacity = true;
		} else {
			Cache.extraCapacity = false;
		}
		if(config.contains("extra-capacity-cost")) {
			Cache.extraCapacityCost = config.getDouble("extra-capacity-cost");
		} else {
			Cache.extraCapacityCost = 1000.0;
		}
		Cache.maxMemberCapacity = config.getInt("max-extra-capacity-from-members");
		if(config.contains("refund-amount")) {
			Cache.refundPercentage = config.getDouble("refund-amount");
		} else {
			Cache.refundPercentage = 0.8;
		}
	}
}
