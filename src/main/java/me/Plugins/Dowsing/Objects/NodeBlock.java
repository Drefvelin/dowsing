package me.Plugins.Dowsing.Objects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import me.Plugins.Dowsing.Loaders.TypeLoader;

public class NodeBlock {
	String id;
	String resource;
	String block;
	List<NodeType> types = new ArrayList<NodeType>();
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBlock() {
		return block;
	}
	public void setBlock(String block) {
		this.block = block;
	}
	public List<NodeType> getTypes() {
		return types;
	}
	public void setTypes(List<NodeType> types) {
		this.types = types;
	}
	public NodeBlock(String key, ConfigurationSection config) {
		this.id = key;
		this.block = config.getString("block");
		this.resource = config.getString("resource");
		List<NodeType> l = new ArrayList<NodeType>();
		for(String s : config.getStringList("main_types")) {
			l.add(TypeLoader.getByString(s));
		}
		this.types = l;
	}
	public NodeBlock(NodeBlock another) {
		this.id = another.id;
		this.block = another.block;
		this.resource = another.resource;
		for(NodeType t : another.types) {
			this.types.add(new NodeType(t));
		}
	}
}
