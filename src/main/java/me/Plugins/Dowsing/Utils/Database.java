package me.Plugins.Dowsing.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.Plugins.Dowsing.Loaders.BlockLoader;
import me.Plugins.Dowsing.Loaders.PMLoader;
import me.Plugins.Dowsing.Loaders.TypeLoader;
import me.Plugins.Dowsing.Managers.NodeManager;
import me.Plugins.Dowsing.Objects.Node;
import me.Plugins.Dowsing.Objects.NodeBlock;
import me.Plugins.Dowsing.Objects.NodeSlot;
import me.Plugins.Dowsing.Objects.NodeType;
import me.Plugins.SimpleFactions.Managers.FactionManager;
import me.Plugins.SimpleFactions.Objects.Faction;

public class Database {
	private JSONObject json; // org.json.simple
    JSONParser parser = new JSONParser();
	public String getResource(Chunk c) throws IOException {
		File folder = new File("plugins/Dowsing/Resources");
    	for (final File file : folder.listFiles()) {
            if (!file.isDirectory()) {
            	try {
        	  	      FileReader myReader = new FileReader(file);
        	  	      BufferedReader myBufferedReader = new BufferedReader(myReader);
        	  	      
        	  	      String line = myBufferedReader.readLine();
	        	  	    if(line.equalsIgnoreCase(c.toString())) {
	        	  	    	String r = myBufferedReader.readLine();
	        	  	    	myReader.close(); 
	                		return r;
	                	}
        	  	      myReader.close();   
        	  	    } catch (FileNotFoundException e) {
        	  	      System.out.println("An error occurred.");
        	  	      e.printStackTrace();
        	  	    }
            }
    	}
		return null;
	}
	public Boolean hasResource(Chunk c) throws IOException {
		File folder = new File("plugins/Dowsing/Resources");
    	for (final File file : folder.listFiles()) {
            if (!file.isDirectory()) {
            	try {
        	  	      FileReader myReader = new FileReader(file);
        	  	      BufferedReader myBufferedReader = new BufferedReader(myReader);
        	  	      
        	  	      String line = myBufferedReader.readLine();
        	  	      myReader.close();   
	        	  	    if(line.equalsIgnoreCase(c.toString())) {
	                		return true;
	                	}
        	  	    } catch (FileNotFoundException e) {
        	  	      System.out.println("An error occurred.");
        	  	      e.printStackTrace();
        	  	    }
            }
    	}
		return false;
	}
	public Boolean removeResource(String id){
		File folder = new File("plugins/Dowsing/Resources");
    	for (final File file : folder.listFiles()) {
            if (!file.isDirectory()) {
            	if(file.getName().equalsIgnoreCase(id+".txt")) {
            		file.delete();
            	}
            }
    	}
		return false;
	}
	public void saveResource(String id, String c, String r) throws IOException {
		File file = new File("plugins/Dowsing/Resources/"+id+".txt");
		if(file.exists()) {
			file.delete();
			file.createNewFile();
		}
		FileWriter fileWriter = new FileWriter(file);
        BufferedWriter myWriter = new BufferedWriter(fileWriter);
        myWriter.write(c);
        myWriter.newLine();
        myWriter.write(r);
        myWriter.newLine();
        myWriter.close();
	}
	public void loadNodes() {
		File folder = new File("plugins/Dowsing/Nodes");
    	for (final File file : folder.listFiles()) {
            if (!file.isDirectory()) {
            	try {
    				json = (JSONObject) parser.parse(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    				Location loc = new Location(Bukkit.getServer().getWorld((String) json.get("world")), (Double) json.get("xPos"),(Double) json.get("yPos"),(Double) json.get("zPos"));
    				UUID id = UUID.fromString((String) json.get("id"));
    				NodeBlock b = BlockLoader.getByString((String) json.get("block"));
    				if(b == null) continue;
    				Faction f = null;
    				if(!((String) json.get("faction")).equalsIgnoreCase("none")) {
    					f = FactionManager.getByString((String) json.get("faction"));
    				}
    				Boolean isActive = Boolean.parseBoolean((String) json.get("active"));
    				int level = (int) Math.round((Double) json.get("level"));
    				int cycleTime = (int) Math.round((Double) json.get("cycle time"));
    				int timeLeft = (int) Math.round((Double) json.get("time remaining"));
    				int inputCounter = (int) Math.round((Double) json.get("input counter"));
    				NodeType currentType = TypeLoader.getByString((String) json.get("current type"));
    				if(currentType == null) continue;
    				List<String> activePMs = new ArrayList<String>();
    				int i = 0;
    				JSONArray pmArray = (JSONArray) json.get("active pms");
    				while(i < pmArray.size()) {
    					activePMs.add(pmArray.get(i).toString());
    					i++;
    				}
    				setSlots(currentType, activePMs);
    				Node n = new Node(id, b, loc, f, isActive, level, cycleTime, currentType, timeLeft, inputCounter);
    				NodeManager.nodes.add(n);
    			} catch (Exception ex) {
    				ex.printStackTrace();
    			}
            }
        }
	}
	private void setSlots(NodeType t, List<String> pms) {
		for(String pm : pms) {
			String type = pm.split("\\.")[0];
			for(NodeSlot slot : t.getSlots()) {
				if(slot.getId().equalsIgnoreCase(type)) {
					slot.setActivePm(PMLoader.getByString(pm.split("\\.")[1]));
					System.out.println(slot.getActivePm().getId());
				}
			}
		}
	}
	public void deleteDatabase() {
    	File folder = new File("plugins/Dowsing/Nodes");
    	for (final File file : folder.listFiles()) {
            if (!file.isDirectory()) {
            	file.delete();
            }
    	}
    }
	@SuppressWarnings("unchecked")
	public void saveNode(Node n) {
		try {
			File file = new File("plugins/Dowsing/Nodes",n.getId()+".json");
			file.createNewFile();
        	PrintWriter pw = new PrintWriter(file, "UTF-8");
        	pw.print("{");
        	pw.print("}");
        	pw.flush();
        	pw.close();
            HashMap<String, Object> defaults = new HashMap<String, Object>();
        	json = (JSONObject) parser.parse(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        	defaults.put("id", n.getId().toString());
        	defaults.put("world", n.getLoc().getWorld().toString().replace("CraftWorld{name=", "").replace("}", ""));
        	defaults.put("xPos", n.getLoc().getX());
        	defaults.put("yPos", n.getLoc().getY());
        	defaults.put("zPos", n.getLoc().getZ());
        	defaults.put("block", n.getBlock().getId());
        	if(n.hasFaction()) {
        		defaults.put("faction", n.getFaction().getId());
        	} else {
        		defaults.put("faction", "none");
        	}
        	defaults.put("active", n.getIsActive().toString());
        	defaults.put("level", n.getLevel());
        	defaults.put("cycle time", n.getCycleTime());
        	defaults.put("time remaining", n.getTimeLeft());
        	defaults.put("input counter", n.getInputCounter());
        	defaults.put("current type", n.getCurrentType().getId());
        	int i = 0;
        	JSONArray pmArray = new JSONArray();
        	while(i < n.getCurrentType().getSlots().size()) {
        		pmArray.add(n.getCurrentType().getSlots().get(i).getId()+"."+n.getCurrentType().getSlots().get(i).getActivePm().getId());
        		i++;
        	}
        	defaults.put("active pms", pmArray);
        	save(file, defaults);
        } catch (Throwable ex) {
			ex.printStackTrace();
        }
	}
	@SuppressWarnings("unchecked")
	public boolean save(File file, HashMap<String, Object> defaults) {
	  try {
		  JSONObject toSave = new JSONObject();
	  
	    for (String s : defaults.keySet()) {
	      Object o = defaults.get(s);
	      if (o instanceof String) {
	        toSave.put(s, getString(s, defaults));
	      } else if (o instanceof Double) {
	        toSave.put(s, getDouble(s, defaults));
	      } else if (o instanceof Integer) {
	        toSave.put(s, getInteger(s, defaults));
	      } else if (o instanceof JSONObject) {
	        toSave.put(s, getObject(s, defaults));
	      } else if (o instanceof JSONArray) {
	        toSave.put(s, getArray(s, defaults));
	      }
	    }
	  
	    TreeMap<String, Object> treeMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
	    treeMap.putAll(toSave);
	  
	   Gson g = new GsonBuilder().setPrettyPrinting().create();
	   String prettyJsonString = g.toJson(treeMap);
	  
	    FileWriter fw = new FileWriter(file);
	    fw.write(prettyJsonString);
	    fw.flush();
	    fw.close();
	  
	    return true;
	  } catch (Exception ex) {
	    ex.printStackTrace();
	    return false;
	  }
	}
	
	public String getRawData(String key, HashMap<String, Object> defaults) {
	    return json.containsKey(key) ? json.get(key).toString()
	       : (defaults.containsKey(key) ? defaults.get(key).toString() : key);
	  }
	
	  public String getString(String key, HashMap<String, Object> defaults) {
	    return ChatColor.translateAlternateColorCodes('&', getRawData(key, defaults));
	  }
	
	  public boolean getBoolean(String key, HashMap<String, Object> defaults) {
	    return Boolean.valueOf(getRawData(key, defaults));
	  }
	
	  public double getDouble(String key, HashMap<String, Object> defaults) {
	    try {
	      return Double.parseDouble(getRawData(key, defaults));
	    } catch (Exception ex) { }
	    return -1;
	  }
	
	  public double getInteger(String key, HashMap<String, Object> defaults) {
	    try {
	      return Integer.parseInt(getRawData(key, defaults));
	    } catch (Exception ex) { }
	    return -1;
	  }
	 
	  public JSONObject getObject(String key, HashMap<String, Object> defaults) {
	     return json.containsKey(key) ? (JSONObject) json.get(key)
	       : (defaults.containsKey(key) ? (JSONObject) defaults.get(key) : new JSONObject());
	  }
	 
	  public JSONArray getArray(String key, HashMap<String, Object> defaults) {
		     return json.containsKey(key) ? (JSONArray) json.get(key)
		       : (defaults.containsKey(key) ? (JSONArray) defaults.get(key) : new JSONArray());
	  }
}
