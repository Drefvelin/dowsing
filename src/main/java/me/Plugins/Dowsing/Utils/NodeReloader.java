package me.Plugins.Dowsing.Utils;

import java.util.HashMap;

import me.Plugins.Dowsing.Loaders.PMLoader;
import me.Plugins.Dowsing.Objects.Node;
import me.Plugins.Dowsing.Objects.NodeSlot;

public class NodeReloader {
	private HashMap<String, String> pmMap = new HashMap<>();
	
	public void cache(Node n) {
		for(NodeSlot slot : n.getCurrentType().getSlots()) {
			pmMap.put(slot.getId(), slot.getActivePm().getId());
		}
	}
	public void reload(Node n) {
		for(NodeSlot slot : n.getCurrentType().getSlots()) {
			if(pmMap.containsKey(slot.getId())){
				slot.setActivePm(PMLoader.getByString(pmMap.get(slot.getId())));
			}
		}
	}
}
