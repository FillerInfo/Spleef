package com.javabean.spleef;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerGameData{
	private Player player;
	private Location locationBeforeGame;
	
	//blocks of the floor destroyed
//	private int blocksDestroyed = 0;
	//measured in game ticks
//	private int timeLasted = 0;
		
	public PlayerGameData(Player p){
		player = p;
		locationBeforeGame = new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), p.getLocation().getYaw(), p.getLocation().getPitch());
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public Location getLocationBeforeGame(){
		return locationBeforeGame;
	}
}
