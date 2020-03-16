package com.javabean.spleef;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class SpleefGame{
	private Arena arena;

	//creates instance of inner class GameTimer
	GameTimer gameTimer = new GameTimer();
	
	//the plugin
	private Plugin plugin;
	
	//there is a game going on or game is being reset, players may not join
	private boolean inProgress = false;
	//game ended, game over for players
	private boolean gameEnded = false;
	
	//time elapsed since game started
	private int timeElapsed = 0;
	//game length in seconds
	//TODO change this to 5 minutes after testing
	private int gameLength = 60;
	
	//map with player name and their data for that game
	private HashMap<String, PlayerGameData> playerData = new HashMap<String, PlayerGameData>();
	
	//after losing, player name added here
	private HashMap<String, Player> losers = new HashMap<String, Player>();
	
	public SpleefGame(Arena a, Plugin p){
		arena = a;
		plugin = p;
	}
	
	//inner class for game timer
	class GameTimer extends BukkitRunnable{
		
		@Override
		public void run(){
			timeElapsed++;
			//game over
			if(timeElapsed == gameLength){
				makeSoundAtPlayers(Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
				//end game
				end();
			}
			//5 second countdown
			else if(timeElapsed + 5 >= gameLength){
				notifyPlayers(ChatColor.DARK_AQUA + "" + (gameLength - timeElapsed) + " seconds left.");
				makeSoundAtPlayers(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
			}
			else if(timeElapsed + 60 == gameLength){
				notifyPlayers(ChatColor.DARK_AQUA + "1 minute left in arena: " + arena.getName());
				makeSoundAtPlayers(Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 2.0f);
			}
			else if(timeElapsed + 120 == gameLength){
				notifyPlayers(ChatColor.DARK_AQUA + "2 minutes left in arena: " + arena.getName());
				makeSoundAtPlayers(Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 2.0f);
			}
		}
	}
	
	//inner class for filling blocks
	class BlockFiller extends BukkitRunnable{
		private Floor floor;
		
		//first corner is least X, Y, Z coord
		private Location[] corners = new Location[2];
		
		//size of one layer of the floor along X, Z plane
		private Point floor2DSize;
		
		//size of one subsection along X, Z plane
		int dimension = 10;
		
		//how many (dimension * dimension) 2d sections of blocks that are filled every tick
		private Point filledCoords;
		private int floorsFilled = 0;
		//areas along edges that are not quite dimension * dimension in size
		private int xCount;
		private int zCount;
		private int floorHeight;
		
		public BlockFiller(Floor f, boolean l){
			floor = f;
			//get correct corners
			corners[0] = floor.getBounds()[0];
			corners[1] = floor.getBounds()[1];
			
			//corner 0 will have lowest x, y, and z location
			if(corners[0].getX() > corners[1].getY()){
				swapX(corners[0], corners[1]);
			}
			else if(corners[0].getY() > corners[1].getY()){
				swapY(corners[0], corners[1]);
			}
			else if(corners[0].getZ() > corners[1].getY()){
				swapZ(corners[0], corners[1]);
			}
			//get number of subsections (fills 1 per tick)
			floor2DSize = new Point((int)(corners[1].getBlockX() - corners[0].getBlockX()), (int)(corners[1].getBlockZ() - corners[0].getBlockZ()));
			floorHeight = (corners[1].getBlockY() - corners[0].getBlockY()) + 1;
			xCount = (floor2DSize.x / dimension);
			zCount = (floor2DSize.y / dimension);
			filledCoords = new Point(0, 0);
		}
		
		public void swapX(Location loc1, Location loc2){
			double temp = loc1.getX();
			loc1.setX(loc2.getX());
			loc2.setX(temp);
		}
		
		public void swapY(Location loc1, Location loc2){
			double temp = loc1.getY();
			loc1.setY(loc2.getY());
			loc2.setY(temp);
		}
		
		public void swapZ(Location loc1, Location loc2){
			double temp = loc1.getZ();
			loc1.setZ(loc2.getZ());
			loc2.setZ(temp);
		}
		
		public int getTickTotal(){
			return (xCount + 1) * (zCount + 1) + floorHeight;
		}
		
		@Override
		public void run(){
			if(floorsFilled == floorHeight){
				//mark as not in progress anymore
				inProgress = false;
				
				//stop this BukkitRunnable
				cancel();
				return;
			}
			
			Location startLoc = new Location(corners[0].getWorld(),
					corners[0].getBlockX() + (filledCoords.x * dimension),
					corners[0].getBlockY() + floorsFilled,
					corners[0].getBlockZ() + (filledCoords.y * dimension));
			
			//reached corner leftover
			if(filledCoords.x == xCount && filledCoords.y == zCount){
				//fill last leftover (corner)
				fillLeftover(startLoc,
						corners[1].getBlockX() - (corners[0].getBlockX() + (filledCoords.x * dimension)),
						corners[1].getBlockZ() - (corners[0].getBlockZ() + (filledCoords.y * dimension)));
				//reset rows and cols for next floor
				filledCoords.x = 0;
				filledCoords.y = 0;
				//increase foorsFilled for next floor
				floorsFilled++;
			}
			//reached end of normal row
			else if(filledCoords.x == xCount){
				//fill leftover
				fillLeftover(startLoc,
						corners[1].getBlockX() - (corners[0].getBlockX() + (filledCoords.x * dimension)),
						dimension);
				//reset for next row
				filledCoords.x = 0;
				filledCoords.y++;
			}
			//filling in leftover row
			else if(filledCoords.y == zCount){
				fillLeftover(startLoc,
						dimension,
						corners[1].getBlockZ() - (corners[0].getBlockZ() + (filledCoords.y * dimension)));
				filledCoords.x++;
			}
			//filling in normal subsection
			else{
				fillSubsection(startLoc);
				filledCoords.x++;
			}
		}
		
		public void fillSubsection(Location startLoc){
			for(int x = 0; x < dimension; x++){
				for(int z = 0; z < dimension; z++){
					replaceBlockIfAir(startLoc);
					startLoc.setZ(startLoc.getBlockZ() + 1);
				}
				startLoc.setZ(startLoc.getBlockZ() - dimension);
				startLoc.setX(startLoc.getBlockX() + 1);
			}
		}
		
		public void fillLeftover(Location startLoc, int xSize, int zSize){
			for(int x = 0; x < xSize; x++){
				for(int z = 0; z < zSize; z++){
					replaceBlockIfAir(startLoc);
					startLoc.setZ(startLoc.getBlockZ() + 1);
				}
				startLoc.setZ(startLoc.getBlockZ() - zSize);
				startLoc.setX(startLoc.getBlockX() + 1);
			}
		}
		
		public void replaceBlockIfAir(Location block){
			//ONLY REPLACE AIR!!!
			if(block.getBlock().getType() == Material.AIR){
				block.getBlock().setType(floor.getFloorMaterial());
			}
		}
	}
	
	public void setArena(Arena a){
		arena = a;
	}
	
	public Arena getArena(){
		return arena;
	}
	
	@SuppressWarnings("deprecation")
	public void start(){
		inProgress = true;
		//prepare players for game
		int playersSetup = 0;
		for(String playerName : playerData.keySet()){
			PlayerGameData playerGameData = playerData.get(playerName);
			Player player = playerGameData.getPlayer();
			
			player.teleport(arena.getSpawns().get(playersSetup).getLocation());
			
			makeSoundAtPlayers(Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
			
			player.getInventory().addItem(new ItemStack(Material.DIAMOND_SHOVEL, 1));
			
			player.setHealth(player.getMaxHealth());
			playersSetup++;
		}
		//runs task immediately and every 20 ticks
		gameTimer.runTaskTimer(plugin, 0, 20);
		notifyPlayers(ChatColor.AQUA + "Game started in arena: " + arena.getName() + " for " + secondsToWords(gameLength));
	}
	
	@SuppressWarnings("deprecation")
	public void end(){
		gameEnded = true;
		gameTimer.cancel();
		notifyPlayers(ChatColor.AQUA + "Game ended in arena: " + arena.getName());
		ArrayList<String> avoidModificationWhileIterating = new ArrayList<String>();
		
		PlayerGameData winner = null;
		for(String playerName : playerData.keySet()){
			if(!losers.containsKey(playerName)){
				winner = playerData.get(playerName);
				break;
			}
		}
		
		if(winner != null){
			winner.setTimeLasted(timeElapsed);
		}
		
		for(String playerName : playerData.keySet()){
			PlayerGameData playerGameData = playerData.get(playerName);
			Player player = playerGameData.getPlayer();
			
			for(PotionEffect effect : player.getActivePotionEffects()){
				player.removePotionEffect(effect.getType());
			}
			
			//notify players it's over and tell them about their team game stats and personal stats
			player.sendMessage(ChatColor.GOLD + "--------------------------------------------");
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "              ***Game Stats***");
			if(winner != null){
				player.sendMessage(ChatColor.AQUA + winner.getPlayer().getName() + " won!");
			}
			else{
				player.sendMessage(ChatColor.RED + "Nobody won...");
			}
			player.sendMessage(ChatColor.GREEN + "You destroyed " + playerGameData.getBlocksDestroyed() + " blocks.");
			player.sendMessage(ChatColor.GREEN + "Time lasted: " + secondsToWords(playerGameData.getTimeLasted()));
			player.sendMessage(ChatColor.GOLD + "--------------------------------------------");
			avoidModificationWhileIterating.add(playerName);
			player.setHealth(player.getMaxHealth());
		}
		
		//kick all players
		for(String playerName : avoidModificationWhileIterating){
			playerLeave(playerData.get(playerName).getPlayer());
		}
		
		int tickDelay = 0;
		//fill floor spaces with Material
		for(Floor floor : arena.getFloors()){
			//runs task after tickDelay every tick after
			BlockFiller bf = new BlockFiller(floor, arena.getFloors().getLast() == floor ? true : false);
			bf.runTaskTimer(plugin, tickDelay, 1);
			tickDelay += bf.getTickTotal();
		}
	}
	
	public String timeLeft(){
		return secondsToWords(gameLength - timeElapsed);
	}
	
	public int getGameLength(){
		return gameLength;
	}
	
	public int getTimeElapsed(){
		return timeElapsed;
	}
	
	public static String secondsToWords(int seconds){
		StringBuilder sb = new StringBuilder();
		int hours = seconds / 3600;
		if(hours > 0){
			if(hours < 10){
				sb.append("0");
			}
			sb.append(hours + ":");
		}
		seconds %= 3600;
		int minutes = seconds / 60;
		if(minutes < 10){
			sb.append("0");
		}
		sb.append(minutes + ":");
		seconds %= 60;
		if(seconds < 10){
			sb.append("0");
		}
		sb.append(seconds);
		return sb.toString();
	}
	
	public boolean isInProgress(){
		return inProgress;
	}
	
	public boolean hasGameEnded(){
		return gameEnded;
	}
	
	public void playerJoin(Player player){
		System.out.println(player.getName() + " joined " + arena.getName());
		playerData.putIfAbsent(player.getName(), new PlayerGameData(player));
		
		player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
		
		SpleefGameManager.playersInArena.put(player.getName(), arena);
	}
	
	public void playerLeave(Player player){
		//clear inventory
		player.getInventory().clear();
		//send player back to location before game
		player.teleport(playerData.get(player.getName()).getLocationBeforeGame());
		playerData.remove(player.getName());
		losers.remove(player.getName());
		
		SpleefGameManager.playersInArena.remove(player.getName());
		
		//all players left except 1
		if(playerData.size() == 1 && !gameEnded){
			end();
		}
	}
	
	public void addLoser(String playerName){
		losers.put(playerName, playerData.get(playerName).getPlayer());
		if(losers.size() == playerData.size() - 1){
			end();
		}
	}
	
	public boolean isALoser(String playerName){
		return losers.containsKey(playerName);
	}
	
	public boolean isPlayerInGame(Player player){
		return playerData.get(player.getName()) != null;
	}
	
	public PlayerGameData getPlayerGameData(String playerName){
		return playerData.get(playerName);
	}
	
	public int getNumPlayers(){
		return playerData.size();
	}
	
	public boolean isFull(){
		return playerData.size() == arena.getSpawns().size();
	}
	
	public Player getPlayer(String playerName){
		return playerData.get(playerName).getPlayer();
	}
	
	//send all players in this game a message
	public void notifyPlayers(String message){
		for(String playerName : playerData.keySet()){
			playerData.get(playerName).getPlayer().sendMessage(message);
		}
	}
	
	//send all players in this game a message
	public void makeSoundAtPlayers(Sound sound, float volume, float pitch){
		for(String playerName : playerData.keySet()){
			Player player = playerData.get(playerName).getPlayer();
			player.playSound(player.getLocation(), sound, volume, pitch);
		}
	}
	
	public String getInfo(){
		StringBuilder sb = new StringBuilder();
		sb.append("Players in game: ");
		//TODO playerdata size not showing
		sb.append(playerData.size());
		return sb.substring(0, sb.length());
	}
}
