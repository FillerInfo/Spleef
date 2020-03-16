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
	
	public boolean containsBlock(Location block){
		int xMin, xMax, yMin, yMax, zMin, zMax;
		
		if(bounds[0].getBlockX() < bounds[1].getBlockX()){
			xMin = bounds[0].getBlockX();
			xMax = bounds[1].getBlockX();
		}
		else{
			xMin = bounds[1].getBlockX();
			xMax = bounds[0].getBlockX();
		}
		
		if(bounds[0].getBlockY() < bounds[1].getBlockY()){
			yMin = bounds[0].getBlockY();
			yMax = bounds[1].getBlockY();
		}
		else{
			yMin = bounds[1].getBlockY();
			yMax = bounds[0].getBlockY();
		}
		
		if(bounds[0].getBlockZ() < bounds[1].getBlockZ()){
			zMin = bounds[0].getBlockZ();
			zMax = bounds[1].getBlockZ();
		}
		else{
			zMin = bounds[1].getBlockZ();
			zMax = bounds[0].getBlockZ();
		}
		
		return xMin <= block.getBlockX()
				&& block.getBlockX() <= xMax
				&& yMin <= block.getBlockY()
				&& block.getBlockY() <= yMax
				&& zMin <= block.getBlockZ()
				&& block.getBlockZ() <= zMax;
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
