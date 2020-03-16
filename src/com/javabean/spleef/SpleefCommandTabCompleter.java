package com.javabean.spleef;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class SpleefCommandTabCompleter implements TabCompleter{
	
	private HashMap<String, Arena> arenaMap = new HashMap<String, Arena>();
	
	public SpleefCommandTabCompleter(HashMap<String, Arena> am){
		arenaMap = am;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
		LinkedList<String> options = new LinkedList<String>();
		if(sender instanceof Player){
			Player player = (Player)sender;
			if(args.length > 0){
				if(args[0].equalsIgnoreCase("info")){
					//do nothing
				}
				else if(args[0].equalsIgnoreCase("join")){
					if(args.length > 1){
						for(String arenaName : arenaMap.keySet()){
							options.add(arenaName);
						}
					}
				}
				else if(args[0].equalsIgnoreCase("leave")){
					//do nothing
				}
				else if(args[0].equalsIgnoreCase("start")){
					//do nothing
				}
				else if(args[0].equalsIgnoreCase("timeleft")){
					//do nothing
				}
				else if(args.length == 1){
					String[] possible = {"info", "join", "leave", "start", "timeleft"};
					addIfStartsWith(options, possible, args[0]);
					if(player.isOp()){
						String[] opPossible = {"addarena", "arena", "removearena"};
						addIfStartsWith(options, opPossible, args[0]);
					}
					return options;
				}
				if(player.isOp()){
					if(args[0].equalsIgnoreCase("addarena")){
						//do nothing
					}
					else if(args[0].equalsIgnoreCase("arena")){
						if(args.length > 1){
							if(args[1].equalsIgnoreCase("addsfloor")){
								if(args.length == 3){
									for(String arenaName : arenaMap.keySet()){
										if(arenaName.startsWith(args[2])){
											options.add(arenaName);
										}
									}
								}
								else if(args.length == 5){
									options.add("<minecraft material id>");
								}
							}
							else if(args[1].equalsIgnoreCase("addspawn") && args.length == 3){
								for(String arenaName : arenaMap.keySet()){
									if(arenaName.startsWith(args[2])){
										options.add(arenaName);
									}
								}
							}
							else if(args[1].equalsIgnoreCase("createsign") && args.length == 3){
								for(String arenaName : arenaMap.keySet()){
									if(arenaName.startsWith(args[2])){
										options.add(arenaName);
									}
								}
							}
							else if(args[1].equalsIgnoreCase("info") && args.length == 3){
								for(String arenaName : arenaMap.keySet()){
									if(arenaName.startsWith(args[2])){
										options.add(arenaName);
									}
								}
							}
							else if(args[1].equalsIgnoreCase("removespawn")){
								if(args.length == 3){
									for(String arenaName : arenaMap.keySet()){
										if(arenaName.startsWith(args[2])){
											options.add(arenaName);
										}
									}
								}
								if(args.length == 4 && arenaMap.get(args[2]) != null){
									Arena arena = arenaMap.get(args[2]);
									for(Spawn spawn: arena.getSpawns()){
										if(spawn.getName().startsWith(args[3])){
											options.add(spawn.getName());
										}
									}
								}
							}
							else if(args[1].equalsIgnoreCase("removesign")){
								//do nothing
							}
							else if(args[1].equalsIgnoreCase("setbound")){
								if(args.length == 3){
									for(String arenaName : arenaMap.keySet()){
										if(arenaName.startsWith(args[2])){
											options.add(arenaName);
										}
									}
								}
								else if(args.length == 4 && arenaMap.get(args[2]) != null){
									Arena arena = arenaMap.get(args[2]);
									for(Floor floor: arena.getFloors()){
										if(floor.getName().startsWith(args[3])){
											options.add(floor.getName());
										}
									}
								}
								else if(args.length == 5){
									options.add("0");
									options.add("1");
								}
							}
							else{
								String[] possible = {"addfloor", "addspawn", "createsign", "info", "removespawn","removesign", "setbound"};
								addIfStartsWith(options, possible, args[1]);
								return options;
							}
						}
					}
					else if(args[0].equalsIgnoreCase("removearena")){
						if(args.length == 2){
							for(String arenaName : arenaMap.keySet()){
								if(arenaName.startsWith(args[1])){
									options.add(arenaName);
								}
							}
						}
					}
				}
			}
		}
		return options;
	}
	
	private void addIfStartsWith(LinkedList<String> options, String[] possible, String arg){
		for(String option : possible){
			if(option.startsWith(arg)){
				options.add(option);
			}
		}
	}
}
