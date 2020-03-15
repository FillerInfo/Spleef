package com.javabean.spleef;

import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

public class Arena {
	private String arenaName;
	private LinkedList<Floor> floors;
	//spawn name, Spawn
	//used for O(1) get spawn
	private LinkedList<Spawn> spawns;
	
	//sign locations
	private LinkedList<Location> signLocations = new LinkedList<Location>();
	
	public static Material[] wallSignTypes = {Material.OAK_WALL_SIGN, Material.ACACIA_WALL_SIGN, Material.BIRCH_WALL_SIGN, Material.DARK_OAK_WALL_SIGN, Material.JUNGLE_WALL_SIGN, Material.SPRUCE_WALL_SIGN};
	
	//for use with addarena
	public Arena(String name){
		//might need to add some default info to avoid errors later
		this(name, new LinkedList<Floor>(), new LinkedList<Spawn>());
	}
	
	//loading from file
	public Arena(String name, LinkedList<Floor> f, LinkedList<Spawn> s){
		arenaName = name;
		floors = f;
		spawns = s;
	}
	
	public String getName(){
		return arenaName;
	}
	
	public LinkedList<Floor> getFloors(){
		return floors;
	}
	
	public Floor getFloor(String floorName){
		for(Floor floor : floors){
			if(floorName.equals(floor.getName())){
				return floor;
			}
		}
		return null;
	}
	
	public void addFloor(Floor floor){
		floors.add(floor);
	}
	
	public LinkedList<Spawn> getSpawns(){
		return spawns;
	}
	
	public Spawn getSpawn(String spawnName){
		for(Spawn spawn: spawns){
			if(spawnName.equals(spawn.getName())){
				return spawn;
			}
		}
		return null;
	}
	
	public void addSpawn(Spawn spawn){
		spawns.add(spawn);
	}
	
	public void removeSpawn(Spawn spawn){
		spawns.remove(spawn);
	}
	
	//call this before allowing a players to play in this arena
	public boolean isPlayable(){
		return floors.size() > 0 && spawns.size() >= 2;
	}
	
	public int numFloors(){
		return floors.size();
	}
	
	public int numSpawns(){
		return spawns.size();
	}
	
	public void createJoinSign(Player player, Plugin plugin){
		if(createJoinSign(Spleef.getBlockLookingAt(player), plugin)){
			player.sendMessage(ChatColor.GREEN + "Sign created successfully.");
		}
		else{
			player.sendMessage(ChatColor.RED + "You must be looking at an existing sign.");
		}
	}
	
	//returns true if it is a sign and created successfully
	public boolean createJoinSign(Location lookingAt, Plugin plugin){
		//if is a sign
		if(isASign(lookingAt.getBlock().getType())){
			Block signBlock = lookingAt.getBlock();
			BlockState signBlockState = signBlock.getState();
			Sign sign = (Sign)signBlockState;
			sign.setLine(0, ChatColor.DARK_RED + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "[Spleef]");
			sign.setLine(1, ChatColor.AQUA + "" + ChatColor.BOLD + "" + getName());
			sign.setLine(2, ChatColor.DARK_GREEN + "" + numSpawns() + " players");
			sign.setMetadata("spleefarena", new FixedMetadataValue(plugin, getName()));
			sign.update();
			signBlockState.update();
			signLocations.add(lookingAt);
			return true;
		}
		return false;
	}
	
	public void removeJoinSign(Location lookingAt, Plugin plugin){
		Iterator<Location> signIterator = signLocations.iterator();
		while(signIterator.hasNext()){
			Location signLocation = signIterator.next();
			if(lookingAt.getWorld().getName() == signLocation.getWorld().getName() && lookingAt.getX() == signLocation.getX() && lookingAt.getY() == signLocation.getY() && lookingAt.getZ() == signLocation.getZ()){
				signLocation.getBlock().getState().removeMetadata("joinarena", plugin);
				signIterator.remove();
			}
		}
	}
	
	public boolean isAJoinSign(Location lookingAt){
		for(Location signLocation : signLocations){
			if(lookingAt.getWorld().getName() == signLocation.getWorld().getName() && lookingAt.getX() == signLocation.getX() && lookingAt.getY() == signLocation.getY() && lookingAt.getZ() == signLocation.getZ()){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isASign(Material materialInQuestion){
		for(Material wallSignType : wallSignTypes){
			if(wallSignType == materialInQuestion){
				return true;
			}
		}
		return false;
	}
	
	public LinkedList<Location> getSigns(){
		
		return signLocations;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Arena: ");
		sb.append(arenaName);
		sb.append(", Floors: ");
		for(Floor floor : floors){
			sb.append(floor);
			sb.append(", ");
		}
		sb.append("Players: " + numSpawns());
		return sb.toString();
	}
}
