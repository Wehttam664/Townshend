package me.capit.Townshend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import me.capit.Townshend.aegis.Aegis;
import me.capit.Townshend.aegis.Aegis.AegisCreationException;
import me.capit.Townshend.group.Group;
import me.capit.Townshend.group.Group.GroupCreationException;
import me.capit.Townshend.group.GroupCommands;
import me.capit.Townshend.town.Town;
import me.capit.Townshend.town.Town.TownCreationException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class TownshendPlugin extends JavaPlugin {
	public static ArrayList<Town> towns = new ArrayList<Town>();
	public static ArrayList<Aegis> aegises = new ArrayList<Aegis>();
	public static ArrayList<Group> groups = new ArrayList<Group>();
	public static Logger logger = null;
	public static ConsoleCommandSender console = null;
	
	public static final String header = ChatColor.DARK_GRAY+"#"+ChatColor.YELLOW+"-----"+
			ChatColor.DARK_GRAY+"["+ChatColor.WHITE+" Townshend "+ChatColor.DARK_GRAY+"]"
			+ChatColor.YELLOW+"------------------------------------------"+ChatColor.DARK_GRAY+"#";
	public static final String footer = ChatColor.DARK_GRAY+"#"+
			ChatColor.YELLOW+"------------------------------------------------------------"+
			ChatColor.DARK_GRAY+"#";
	
	public static final String gHeader = ChatColor.DARK_GRAY+"#"+ChatColor.YELLOW+"-----"+
			ChatColor.DARK_GRAY+"["+ChatColor.WHITE+" Townshend "+ChatColor.DARK_GRAY+"]"
			+ChatColor.YELLOW+"----------------------------------"+ChatColor.DARK_GRAY+"#";
	public static final String gFooter = ChatColor.DARK_GRAY+"#"+
			ChatColor.YELLOW+"---------------------------------------------------"+ChatColor.DARK_GRAY+"#";
	public static final String tag = ChatColor.DARK_GRAY+"["+ChatColor.YELLOW+"Townshend"+
			ChatColor.DARK_GRAY+"] "+ChatColor.WHITE;
	
	public static File townsDir = null;
	public static File groupsDir = null;
	public static File aegisDir = null;
	
	public TownCommands commands = null;
	public GroupCommands gcmds = null;
	
	@Override
	public void onEnable(){
		logger = getLogger();
		console = this.getServer().getConsoleSender();
		
		console.sendMessage(header);
		
		console.sendMessage(ChatColor.WHITE+"Loading "+ChatColor.YELLOW+"file data...");
		saveDefaultConfig();
		reloadConfig();
		
		townsDir = new File(this.getDataFolder().getAbsolutePath()+File.separator+"towns");
		if (!townsDir.exists() || !townsDir.isDirectory()){
			console.sendMessage(ChatColor.WHITE+"Towns directory missing. "+ChatColor.LIGHT_PURPLE+"Creating...");
			townsDir.mkdir();
		}
		for (File town : townsDir.listFiles()){
			try {
				Town t = new Town(this, town.getName());
				towns.add(t);
				console.sendMessage(ChatColor.WHITE+" > Loaded data for "+ChatColor.LIGHT_PURPLE
						+t.getName()+ChatColor.WHITE+" ("+ChatColor.AQUA+t.ID+ChatColor.WHITE+").");
			} catch (TownCreationException e) {
				console.sendMessage(ChatColor.WHITE+" > "+ChatColor.RED+"Error "+ChatColor.WHITE
						+"loading Town from "+ChatColor.AQUA+town.getName()+ChatColor.WHITE+": "+e.getLocalizedMessage());
			}
		}
		
		groupsDir = new File(this.getDataFolder().getAbsolutePath()+File.separator+"groups");
		if (!groupsDir.exists() || !groupsDir.isDirectory()){
			console.sendMessage(ChatColor.WHITE+"Groups directory missing. "+ChatColor.LIGHT_PURPLE+"Creating...");
			groupsDir.mkdir();
		}
		for (File group : groupsDir.listFiles()){
			try {
				Group g = new Group(this, group.getName());
				groups.add(g);
				console.sendMessage(ChatColor.WHITE+" > Loaded group "+ChatColor.LIGHT_PURPLE
						+g.name+ChatColor.WHITE+".");
			} catch (GroupCreationException e) {
				console.sendMessage(ChatColor.WHITE+" > "+ChatColor.RED+"Error "+ChatColor.WHITE
						+"loading Group from "+ChatColor.AQUA+group.getName()+ChatColor.WHITE+": "+e.getLocalizedMessage());
			}
		}
		
		aegisDir = new File(getDataFolder().getAbsolutePath()+File.separator+"aegis");
		if (!aegisDir.exists() || !aegisDir.isDirectory()){
			console.sendMessage(ChatColor.WHITE+"Aegis directory missing. "+ChatColor.LIGHT_PURPLE+"Creating...");
			aegisDir.mkdir();
		}
		for (File aegisFile : aegisDir.listFiles()){
			YamlConfiguration aConfig = YamlConfiguration.loadConfiguration(aegisFile);
			Location center = new Location(
					getServer().getWorld(aConfig.getString("world")),
					aConfig.getDouble("x"), aConfig.getDouble("y"), aConfig.getDouble("z"));
			try {
				Aegis aegis = new Aegis(center, aConfig.getString("name"), aConfig.getInt("ID"));
				aegises.add(aegis);
				console.sendMessage(ChatColor.WHITE+" > Loaded data for Aegis "+ChatColor.LIGHT_PURPLE
						+aegis.getGroup()+ChatColor.WHITE+" ("+ChatColor.AQUA
						+center.getX()+","+center.getY()+","+center.getZ()+ChatColor.WHITE+").");
			} catch (AegisCreationException e){
				console.sendMessage(ChatColor.WHITE+" > "+ChatColor.RED+"Error "+ChatColor.WHITE
						+"loading Aegis at ("+ChatColor.AQUA+center.getX()+","+center.getY()+","
						+center.getZ()+ChatColor.WHITE+"): "+e.getLocalizedMessage());
			}
		}
		
		console.sendMessage(ChatColor.WHITE+"Hooking "+ChatColor.YELLOW+"commands...");
		commands = new TownCommands(this);
		this.getCommand("townshend").setExecutor(commands);
		gcmds = new GroupCommands(this);
		this.getCommand("group").setExecutor(gcmds);
		
		console.sendMessage(ChatColor.WHITE+"Done loading!");
		
		console.sendMessage(footer);
	}
	
	@Override
	public void onDisable(){
		console.sendMessage(header);
		
		console.sendMessage(ChatColor.WHITE+"Wrapping up core data...");
		
		for (Town town : towns){
			town.save();
		}
		
		for (Group group : groups){
			group.save();
		}
		
		for (Aegis aegis : aegises){
			try {
				File aFile = new File(aegisDir+File.separator+aegis.ID+".yml");
				YamlConfiguration aConfig = YamlConfiguration.loadConfiguration(aFile);
				aConfig.set("ID", aegis.ID);
				aConfig.set("world", aegis.getCenter().getWorld().getName());
				aConfig.set("name", aegis.getGroup());
				aConfig.set("x", aegis.getSignLoc().getX());
				aConfig.set("y", aegis.getSignLoc().getY());
				aConfig.set("z", aegis.getSignLoc().getZ());
				aConfig.save(aFile);
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		
		console.sendMessage(ChatColor.WHITE+"Wrapped up.");
		
		console.sendMessage(footer);
	}
	
	public static int getNextID(){
		int lowest = 0;
		for (Town town : towns){
			lowest = town.ID>lowest ? town.ID : lowest;
		}
		return lowest;
	}
	
	public static Town getTownByName(String name){
		for (Town town : towns){
			if (town.getName().equalsIgnoreCase(name)) return town;
		}
		return null;
	}
	
	public static Town getTownOfPlayer(UUID player){
		for (Town town : towns){
			if (town.hasPlayer(player)) return town;
		}
		return null;
	}
	
	public static void deleteTown(Town town){
		for (int i=0;i<towns.size();i++){
			Town t = towns.get(i);
			if (t.ID==town.ID){
				towns.remove(i);
				for (UUID id : t.getPlayers()){
					Messager.sendWarning(Bukkit.getServer().getPlayer(id), "Your town was disbanded.");
				}
				town.FILE.delete();
				break;
			}
		}
	}
	
	public static ArrayList<Group> getGroupsOfPlayer(UUID player){
		ArrayList<Group> gps = new ArrayList<Group>();
		for (Group g : groups){
			if (g.isMember(player)){
				gps.add(g);
			}
		}
		return gps;
	}
	
	public static ArrayList<Group> getGroupsOwnedByPlayer(UUID player){
		ArrayList<Group> gps = new ArrayList<Group>();
		for (Group g : groups){
			if (g.owner==player){
				gps.add(g);
			}
		}
		return gps;
	}
	
	public static Group getGroupByName(String name){
		for (Group g : groups){
			if (g.name.equalsIgnoreCase(name)){
				return g;
			}
		}
		return null;
	}
}
