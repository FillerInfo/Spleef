package com.javabean.spleef;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Spleef extends JavaPlugin{
	
	private HashMap<String, Arena> arenaMap = new HashMap<String, Arena>();
	private SpleefGameManager gameManager = new SpleefGameManager(this);
	
	//TODO javadoc everywhere
	
	// Fired when plugin is first enabled
	@Override
	public void onEnable(){
		//read arena data from XML file
		parseXMLArenaData();
		
		//read sign data from XML file
		parseXMLSignData();
		
		//creates commands
		getCommand("spleef").setExecutor(new TheSpleefCommand(gameManager, arenaMap, this));
		getCommand("spleef").setTabCompleter(new SpleefCommandTabCompleter(arenaMap));
		
		//event listener
		getServer().getPluginManager().registerEvents(new SpleefListener(gameManager, arenaMap), this);
		
		//plugin enabled successfully
		getLogger().info("-----------------------");
		getLogger().info(getClass().getSimpleName() + " enabled!");
		getLogger().info("-----------------------");
	}
	
	// Fired when plugin is disabled
	@Override
	public void onDisable(){
		//rewrite arena data to XML file
		writeArenaDataToXML();
		
		//rewrite sign data to XML file
		writeSignDataToXML();
		
		//plugin disabled successfully
		getLogger().info("------------------------");
		getLogger().info(getClass().getSimpleName() + " disabled!");
		getLogger().info("------------------------");
	}
	
	private World getWorldOfName(List<World> worlds, String name){
		for(World world : worlds){
			if(world.getName().equals(name)){
				return world;
			}
		}
		return null;
	}
	
	private void parseXMLArenaData(){
		//set up file location
		String arenaFileName = "arena.xml";
		File arenaInfoFile = getDataFolder();
		if(arenaInfoFile.mkdir()){
			getLogger().info("Created \\Spleef directory.");
		}
		
		arenaInfoFile = new File(arenaInfoFile.toString() + "\\" + arenaFileName);
		try {
			if(arenaInfoFile.createNewFile()){
				getLogger().info("Created new " + arenaFileName + " file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<World> worlds = Bukkit.getWorlds();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(arenaInfoFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		
		//gets the worlds
		NodeList worldList = doc.getElementsByTagName("world");
		for (int worldIndex = 0; worldIndex < worldList.getLength(); worldIndex++) {
			Node worldNode = worldList.item(worldIndex);
			if (worldNode.getNodeType() == Node.ELEMENT_NODE) {
				Element worldElement = (Element)worldNode;
				String worldName = worldElement.getAttribute("name");
				World world = getWorldOfName(worlds, worldName);
				
				//gets arenas for world
				NodeList arenaList = worldElement.getElementsByTagName("arena");
				for (int arenaIndex = 0; arenaIndex < arenaList.getLength(); arenaIndex++) {
					Node arenaNode = arenaList.item(arenaIndex);
					if (arenaNode.getNodeType() == Node.ELEMENT_NODE) {
						Element arenaElement = (Element)arenaNode;
						String arenaName = arenaElement.getAttribute("name");
						
						//gets the floors for arena
						LinkedList<Floor> floors = new LinkedList<Floor>();
						NodeList floorList = arenaElement.getElementsByTagName("floor");
						for(int floorIndex = 0; floorIndex < floorList.getLength(); floorIndex++){
							Node floorNode = floorList.item(floorIndex);
							if(floorNode.getNodeType() == Node.ELEMENT_NODE){
								Element floorElement = (Element)floorNode;
								String floorName = floorElement.getAttribute("name");
								Location location1 = new Location(world,
										Double.parseDouble(floorElement.getAttribute("x1")),
										Double.parseDouble(floorElement.getAttribute("y1")),
										Double.parseDouble(floorElement.getAttribute("z1")));
								Location location2 = new Location(world,
										Double.parseDouble(floorElement.getAttribute("x2")),
										Double.parseDouble(floorElement.getAttribute("y2")),
										Double.parseDouble(floorElement.getAttribute("z2")));
								Location[] locs = {location1, location2};
								floors.add(new Floor(floorName, locs, Material.getMaterial(floorElement.getAttribute("material"))));
							}
						}
						
						//gets the spawns for arena
						LinkedList<Spawn> spawns = new LinkedList<Spawn>();
						NodeList spawnsList = arenaElement.getElementsByTagName("spawn");
						for(int spawnIndex = 0; spawnIndex < spawnsList.getLength(); spawnIndex++){
							Node spawnNode = spawnsList.item(spawnIndex);
							if(spawnNode.getNodeType() == Node.ELEMENT_NODE){
								Element spawnElement = (Element)spawnNode;
								String spawnName = spawnElement.getAttribute("name");
								//new spawn created
								spawns.add(new Spawn(spawnName, new Location(world,
										Double.parseDouble(spawnElement.getAttribute("x")),
										Double.parseDouble(spawnElement.getAttribute("y")),
										Double.parseDouble(spawnElement.getAttribute("z")),
										Float.parseFloat(spawnElement.getAttribute("yaw")),
										Float.parseFloat(spawnElement.getAttribute("pitch")))));
							}
						}
						
						NodeList spectateLocs = arenaElement.getElementsByTagName("spectate");
						Location spectateTeleport = null;
						for(int specIndex = 0; specIndex < spectateLocs.getLength(); specIndex++){
							Node specNode = spectateLocs.item(specIndex);
							if(specNode.getNodeType() == Node.ELEMENT_NODE){
								Element specElement = (Element)specNode;
								//new spawn created
								spectateTeleport = new Location(world,
										Double.parseDouble(specElement.getAttribute("x")),
										Double.parseDouble(specElement.getAttribute("y")),
										Double.parseDouble(specElement.getAttribute("z")),
										Float.parseFloat(specElement.getAttribute("yaw")),
										Float.parseFloat(specElement.getAttribute("pitch")));
							}
						}
						
						//new arena created
						arenaMap.put(arenaName, new Arena(arenaName, floors, spawns, spectateTeleport));
					}
				}
			}
		}
	}
	
	private void writeArenaDataToXML(){
		//set up file location
		String arenaFileName = "arena.xml";
		File arenaInfoFile = getDataFolder();
		if(arenaInfoFile.mkdir()){
			getLogger().info("Created \\Spleef directory.");
		}
		
		arenaInfoFile = new File(arenaInfoFile.toString() + "\\" + arenaFileName);
		try {
			if(arenaInfoFile.createNewFile()){
				getLogger().info("Created new " + arenaFileName + " file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//write to XML
		try {
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			Document doc = docBuilder.newDocument();
			
			//create worlds root tag
			Element worldsRootElement = doc.createElement("worlds");
			doc.appendChild(worldsRootElement);
			for(String arenaName : arenaMap.keySet()){
				Arena arena = arenaMap.get(arenaName);
				World arenaWorld = arena.getFloors().get(0).getWorld();
				
				//get or create world element
				Element worldElement = getOrCreateWorldElement(doc, worldsRootElement, arenaWorld.getName());
				
				//create element for current arena
				Element arenaElement = doc.createElement("arena");
				arenaElement.setAttribute("name", arena.getName());
				worldElement.appendChild(arenaElement);
				
				//create elements for floor info
				for(Floor floor : arena.getFloors()){
					Element floorElement = doc.createElement("floor");
					floorElement.setAttribute("name", floor.getName());
					floorElement.setAttribute("x1", "" + floor.getBounds()[0].getBlockX());
					floorElement.setAttribute("y1", "" + floor.getBounds()[0].getBlockY());
					floorElement.setAttribute("z1", "" + floor.getBounds()[0].getBlockZ());
					floorElement.setAttribute("x2", "" + floor.getBounds()[1].getBlockX());
					floorElement.setAttribute("y2", "" + floor.getBounds()[1].getBlockY());
					floorElement.setAttribute("z2", "" + floor.getBounds()[1].getBlockZ());
					floorElement.setAttribute("material", floor.getFloorMaterial().name());
					arenaElement.appendChild(floorElement);
				}
				
				//create elements for spawn info
				for(Spawn spawn : arena.getSpawns()){
					Element spawnElement = doc.createElement("spawn");
					spawnElement.setAttribute("name", spawn.getName());
					spawnElement.setAttribute("x", "" + spawn.getLocation().getX());
					spawnElement.setAttribute("y", "" + spawn.getLocation().getY());
					spawnElement.setAttribute("z", "" + spawn.getLocation().getZ());
					spawnElement.setAttribute("yaw", "" + spawn.getLocation().getYaw());
					spawnElement.setAttribute("pitch", "" + spawn.getLocation().getPitch());
					arenaElement.appendChild(spawnElement);
				}
				
				Element spectateElement = doc.createElement("spectate");
				spectateElement.setAttribute("x", "" + arena.getSpectateLocation().getX());
				spectateElement.setAttribute("y", "" + arena.getSpectateLocation().getY());
				spectateElement.setAttribute("z", "" + arena.getSpectateLocation().getZ());
				spectateElement.setAttribute("yaw", "" + arena.getSpectateLocation().getYaw());
				spectateElement.setAttribute("pitch", "" + arena.getSpectateLocation().getPitch());
				arenaElement.appendChild(spectateElement);
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(arenaInfoFile);
			
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			
			transformer.transform(source, result);
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
	
	private Element getOrCreateWorldElement(Document doc, Element worldsRootElement, String worldName){
		NodeList worldList = worldsRootElement.getChildNodes();
		for(int i = 0; i < worldList.getLength(); i++){
			Element worldElement = (Element)worldList.item(i);
			if(worldElement.getAttributeNode("name").getValue().equals(worldName)){
				//element for this world already exists, return it
				return worldElement;
			}
		}
		
		//element for this world DNE, create new one
		Element worldElement = doc.createElement("world");
		worldElement.setAttribute("name", worldName);
		worldsRootElement.appendChild(worldElement);
		return worldElement;
	}
	
	public static Location getBlockLookingAt(Player player){
		Set<Material> ignoreBlocks = new HashSet<Material>();
		ignoreBlocks.add(Material.AIR);
		ignoreBlocks.add(Material.WATER);
		ignoreBlocks.add(Material.LAVA);
		return player.getTargetBlock(ignoreBlocks, 100).getLocation();
	}
	
	private void parseXMLSignData(){
		//set up file location
		String arenaFileName = "signs.xml";
		File arenaInfoFile = getDataFolder();
		if(arenaInfoFile.mkdir()){
			getLogger().info("Created \\Spleef directory.");
		}
		
		arenaInfoFile = new File(arenaInfoFile.toString() + "\\" + arenaFileName);
		try {
			if(arenaInfoFile.createNewFile()){
				getLogger().info("Created new " + arenaFileName + " file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<World> worlds = Bukkit.getWorlds();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(arenaInfoFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		
		//get the list of signs
		NodeList signList = doc.getElementsByTagName("sign");
		for (int signIndex = 0; signIndex < signList.getLength(); signIndex++) {
			Node signNOde = signList.item(signIndex);
			if (signNOde.getNodeType() == Node.ELEMENT_NODE) {
				Element signElement = (Element)signNOde;
				String worldName = signElement.getAttribute("world");
				Location signLocation = new Location(getWorldOfName(worlds, worldName),
						Double.parseDouble(signElement.getAttribute("x")),
						Double.parseDouble(signElement.getAttribute("y")),
						Double.parseDouble(signElement.getAttribute("z")));
				String arenaName = signElement.getAttribute("spleefarena");
				arenaMap.get(arenaName).createJoinSign(signLocation, this);
			}
		}
	}
	
	private void writeSignDataToXML() {
		//set up file location
		String arenaFileName = "signs.xml";
		File arenaInfoFile = getDataFolder();
		if(arenaInfoFile.mkdir()){
			getLogger().info("Created \\Spleef directory.");
		}
		
		arenaInfoFile = new File(arenaInfoFile.toString() + "\\" + arenaFileName);
		try {
			if(arenaInfoFile.createNewFile()){
				getLogger().info("Created new " + arenaFileName + " file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//write to XML
		try {
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			Document doc = docBuilder.newDocument();
			
			//create signs root tag
			Element signsRootElement = doc.createElement("signs");
			doc.appendChild(signsRootElement);
			for(String arenaName : arenaMap.keySet()){
				for(Location signLocation : arenaMap.get(arenaName).getSigns()){
					Element signElement = doc.createElement("sign");
					signElement.setAttribute("world", signLocation.getWorld().getName());
					signElement.setAttribute("x", "" + signLocation.getX());
					signElement.setAttribute("y", "" + signLocation.getY());
					signElement.setAttribute("z", "" + signLocation.getZ());
					signElement.setAttribute("spleefarena", arenaName);
					signsRootElement.appendChild(signElement);
				}
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(arenaInfoFile);
			
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			
			transformer.transform(source, result);
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
}