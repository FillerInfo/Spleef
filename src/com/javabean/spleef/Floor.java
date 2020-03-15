package com.javabean.spleef;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class Floor{
	private String name;
	private Location[] bounds = new Location[2];
	private Material floorMaterial;
	
	public Floor(String n, Location[] b, Material fM){
		name = n;
		bounds = b;
		floorMaterial = fM;
	}
	
	public World getWorld(){
		return bounds[0].getWorld();
	}
	
	public Location[] getBounds(){
		return bounds;
	}
	
	public int getXWidth(){
		return (int)Math.abs(bounds[0].getX() - bounds[1].getX());
	}
	
	public int getYWidth(){
		return (int)Math.abs(bounds[0].getY() - bounds[1].getY());
	}
	
	public int getZWidth(){
		return (int)Math.abs(bounds[0].getZ() - bounds[1].getZ());
	}
	
	public void setBound(Location loc, int index){
		bounds[index] = loc;
	}
	
	public String getName(){
		return name;
	}
	
	public Material getFloorMaterial(){
		return floorMaterial;
	}
	
	public void setFloorMaterial(Material fM){
		floorMaterial = fM;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{ Floor bounds: ");
		for(Location loc : bounds){
			sb.append(loc);
		}
		sb.append(" }");
		return sb.toString();
	}
}
