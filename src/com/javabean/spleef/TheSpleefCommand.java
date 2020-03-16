package com.javabean.spleef;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

public class TheSpleefCommand implements CommandExecutor{
	
	//arena name, arena
	private HashMap<String, Arena> arenaMap;
	private SpleefGameManager gameManager;
	private Plugin plugin;
	
	public TheSpleefCommand(SpleefGameManager ctfgm, HashMap<String, Arena> a, Plugin p){
		gameManager = ctfgm;
		arenaMap = a;
		plugin = p;
	}
	
	//TODO quickjoin to join the most full game, else random
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(sender instanceof Player){
			Player player = (Player)sender;
			String commandSoFar = "/spleef";
			
			//no args
			if(args.length == 0){
				if(player.isOp()){
					player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena | addarena | info | join | leave | removearena | spawn | start | team | timeleft>.");
				}
				else{
					player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <info | join | leave | start | team | timeleft>.");
				}
			}
			else if(args[0].equalsIgnoreCase("join")){
				commandSoFar += " " + args[0];
				if(args.length == 2){
					//arena does not exist
					if(arenaMap.get(args[1]) == null){
						player.sendMessage(ChatColor.RED + "Arena: " + args[1] + " does not exist.");
					}
					else{
						gameManager.attemptPlayerJoin(player, arenaMap.get(args[1]));
					}
				}
				else{
					player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments <arena name>");
				}
			}
			else if(args[0].equalsIgnoreCase("leave")){
				Arena playerGameArena = gameManager.getPlayerGameArena(player);
				if(playerGameArena == null){
					player.sendMessage(ChatColor.RED + "You are not in a Spleef game!");
				}
				else{
					//player leaves arena game
					gameManager.notifyPlayers(ChatColor.LIGHT_PURPLE + player.getName() + " left arena: " + playerGameArena.getName(), playerGameArena);
					gameManager.playerLeave(player, playerGameArena);
					player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.5f, 1.0f);
				}
			}
			else if(args[0].equalsIgnoreCase("start")){
				Arena playerGameArena = gameManager.getPlayerGameArena(player);
				if(playerGameArena == null){
					player.sendMessage(ChatColor.RED + "You are not in a Spleef game!");
				}
				else if(gameManager.isInProgress(playerGameArena)){
					player.sendMessage(ChatColor.RED + "Game is already in progress!");
				}
//				TODO insert this back in upon further testing
//				else if(gameManager.getNumPlayersInArena(playerGameArena) < 2){
//					player.sendMessage(ChatColor.RED + "There must be at least two players to start a Spleef game!");
//				}
				else{
					//start game
					gameManager.notifyPlayers(ChatColor.LIGHT_PURPLE + player.getName() + " started the game.", playerGameArena);
					gameManager.startArena(playerGameArena);
				}
			}
			else if(args[0].equalsIgnoreCase("timeleft")){
				Arena playerGameArena = gameManager.getPlayerGameArena(player);
				if(playerGameArena != null){
					player.sendMessage(ChatColor.AQUA + "There is " + gameManager.timeLeft(playerGameArena) + " time left in arena: " + playerGameArena.getName() + ".");
				}
				else{
					player.sendMessage(ChatColor.RED + "The command /spleef timeleft is used while in a Spleef game.");
				}
			}
			else if(args[0].equalsIgnoreCase("info")){
				player.sendMessage(ChatColor.GOLD + "-----------------------------------------------");
				player.sendMessage(ChatColor.GOLD + "              ***Spleef Games Info***");
				if(gameManager.getNumGames() == 0){
					player.sendMessage(ChatColor.RED + "There are no games at the moment.");
					player.sendMessage(ChatColor.LIGHT_PURPLE + "Click a join sign in the hub or /spleef join <arena name>");
				}
				else{
					gameManager.getInfo(player);
					player.sendMessage(ChatColor.LIGHT_PURPLE + "Click to join!");
				}
				player.sendMessage(ChatColor.GOLD + "-----------------------------------------------");
			}
			//incorrect args
			else if(player.isOp()){
				if(args[0].equalsIgnoreCase("arena")){
					arenaSubCommand(player, commandSoFar + " " + args[0], args);
				}
				else if(args[0].equalsIgnoreCase("addarena")){
					addArenaSubCommand(player, commandSoFar + " " + args[0], args);
				}
				else if(args[0].equalsIgnoreCase("removearena")){
					removeArenaSubCommand(player, commandSoFar + " " + args[0], args);
				}
				else{
					player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena | addarena | info | join | leave | removearena | spawn | start | team | timeleft>.");
				}
			}
			else{
				player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <info | join | leave | start | team | timeleft>.");
			}
		}
		return true;
	}
	
	private void arenaSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length >= 2){
			if(args[1].equalsIgnoreCase("info")){
				infoSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("setbound")){
				setBoundSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("addfloor")){
				addFloorSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("addspawn")){
				addSpawnSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("removespawn")){
				removeSpawnSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("setspectate")){
				setSpectateSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("createsign")){
				createSignSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("removesign")){
				removeSignSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else{
				player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <addflag | addspawn | addteam | createsign | info | removeteam | removespawn | removeflag | removesign | setbound>.");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <addflag | addspawn | addteam | createsign | info | removeteam | removespawn | removeflag | removesign | setbound>.");
		}
	}

	private void addArenaSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length == 1){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name>.");
		}
		else if(args.length == 2){
			//create new arena
			//name is specified, location is set to player location by default
			//arena does not exist yet, create it
			if(arenaMap.get(args[1]) == null){
				arenaMap.putIfAbsent(args[1], new Arena(args[1], player.getWorld()));
				player.sendMessage(ChatColor.GREEN + "You added an arena named: " + args[1] + ".");
			}
			else{
				player.sendMessage(ChatColor.RED + "Arena: " + args[1] + " already exists.");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void removeArenaSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length == 1){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name>.");
		}
		else if(args.length == 2){
			arenaMap.remove(args[1]);
			player.sendMessage(ChatColor.GREEN + "You removed the arena named: " + args[1] + ".");
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void infoSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length == 2){
			player.sendMessage(ChatColor.GOLD + "--------------------------------------------");
			player.sendMessage(ChatColor.GOLD + "          ***General Arena Info***");
			if(arenaMap.size() == 0){
				player.sendMessage(ChatColor.RED + "No arenas exist.");
			}
			else{
				for(String arenaName : arenaMap.keySet()){
					player.sendMessage(ChatColor.GREEN + arenaName + ": " +
							arenaMap.get(arenaName).numSpawns() + " spawns, " +
							arenaMap.get(arenaName).numFloors() + " floors.");
				}
			}
			player.sendMessage(ChatColor.GOLD + "--------------------------------------------");
		}
		else if(args.length == 3){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			else{
				player.sendMessage(ChatColor.GREEN + args[2] + ": " +
						arenaMap.get(args[2]).numSpawns() + " spawns, " +
						arenaMap.get(args[2]).numFloors() + " floors.");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void setBoundSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length < 5){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name> <floor name> <0 | 1>.");
		}
		else if(args.length == 5){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//floor does not exist
			else if(arenaMap.get(args[2]).getFloor(args[3]) == null){
				player.sendMessage(ChatColor.RED + "Floor: " + args[3] + " does not exist for Arena: " + args[2] + ".");
			}
			else{
				int bound = Integer.parseInt(args[4]);
				if(bound == 0 || bound == 1){
					arenaMap.get(args[2]).getFloor(args[3]).setBound(Spleef.getBlockLookingAt(player), bound);
					player.sendMessage(ChatColor.GREEN + "You set bound: " + args[3] + " for arena: " + args[2] + ".");
				}
				else{
					player.sendMessage(ChatColor.RED + "Bound specified must be 0 or 1.");
				}
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void addSpawnSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length < 4){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name> <spawn name>.");
		}
		else if(args.length == 4){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//spawn does not exist, create it
			else if(arenaMap.get(args[2]).getSpawn(args[3]) == null){
				arenaMap.get(args[2]).addSpawn(new Spawn(args[3], player.getLocation()));
				player.sendMessage(ChatColor.GREEN + "You added spawn: " + args[3] + " for arena: " + args[2] + ".");
			}
			//spawn exists
			else{
				player.sendMessage(ChatColor.RED + "Spawn: " + args[3] + "already exists for arena: " + args[2] + ".");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void addFloorSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length < 5){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name> <floor name> <Material>.");
		}
		else if(args.length == 5){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//floor does not exist, create it
			else if(arenaMap.get(args[2]).getFloor(args[3]) == null){
				//Material does not exist
				if(Material.getMaterial(args[4]) == null){
					player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
				}
				else{
					Location[] locs = new Location[2];
					arenaMap.get(args[2]).addFloor(new Floor(args[3], locs, Material.getMaterial(args[4])));
					player.sendMessage(ChatColor.GREEN + "You added floor: " + args[3] + " for arena: " + args[2] + ".");
				}
			}
			//floor exists
			else{
				player.sendMessage(ChatColor.RED + "floor: " + args[3] + "already exists for arena: " + args[2] + ".");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void removeSpawnSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length < 5){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name> <team name> <spawn name>.");
		}
		else if(args.length == 5){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//spawn does not exist
			else if(arenaMap.get(args[2]).getSpawn(args[3]) == null){
				player.sendMessage(ChatColor.RED + "Spawn: " + args[4] + "does not exist for arena: " + args[2] + ".");
			}
			//spawn exists, delete it
			else{
				arenaMap.get(args[2]).removeSpawn(arenaMap.get(args[2]).getSpawn(args[3]));
				player.sendMessage(ChatColor.GREEN + "You removed spawn: " + args[4] + " for team: " + args[3] + " for arena: " + args[2] + ".");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void setSpectateSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length == 2){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name>.");
		}
		else if(args.length == 3){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//create sign where the player is looking at
			else{
				Location loc = player.getLocation();
				arenaMap.get(args[2]).setSpectateLocation(new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY(), loc.getBlockZ() + 0.5));
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}

	private void createSignSubCommand(Player player, String commandSoFar, String[] args) {
		if(args.length == 2){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name>.");
		}
		else if(args.length == 3){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//create sign where the player is looking at
			else{
				arenaMap.get(args[2]).createJoinSign(player, plugin);
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void removeSignSubCommand(Player player, String commandSoFar, String[] args) {
		if(args.length == 2){
			Location lookingAt = Spleef.getBlockLookingAt(player);
			boolean removed = false;
			for(String arenaName : arenaMap.keySet()){
				if(arenaMap.get(arenaName).isAJoinSign(lookingAt)){
					arenaMap.get(arenaName).removeJoinSign(lookingAt, plugin);
					removed = true;
				}
			}
			if(removed){
				player.sendMessage(ChatColor.GREEN + "Sign removed successfully.");
			}
			else{
				player.sendMessage(ChatColor.RED + "There is not join sign there.");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
}
