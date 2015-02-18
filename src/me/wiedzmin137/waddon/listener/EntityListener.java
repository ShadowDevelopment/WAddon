package me.wiedzmin137.waddon.listener;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import me.wiedzmin137.waddon.WAddon;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class EntityListener implements Listener {
	
	@SuppressWarnings("unused")
	private WAddon basePlugin;
	private int spawnCap;
	private List<EntityType> allowedEntities;
	private Hashtable<EntityType, Integer> individualCapData;
	private boolean individualEntityCap;
	@SuppressWarnings("unused")
	private WAddon p;
	
	public EntityListener(WAddon plugin, int cap, List<EntityType> allowedEnts, String bmsg)
	{
		this.p = plugin;
		//no individual entity counting
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		spawnCap = cap;
		allowedEntities = allowedEnts;
		individualEntityCap = false;
	}
	
	public EntityListener(WAddon plugin, int cap, Hashtable<EntityType, Integer> capdata, String bmsg)
	{
		//individual entity counting
		basePlugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		individualCapData = capdata;
		allowedEntities = new ArrayList<EntityType>();
		individualEntityCap = true;
		
		Enumeration<EntityType> entityTypes = capdata.keys();
		
		while (entityTypes.hasMoreElements())
		{
			EntityType temp = entityTypes.nextElement();
			allowedEntities.add(temp);
		}
		
		if (allowedEntities.size() == 0)
		{
			plugin.getLogger().info("Entity list is length 0!");
		}

	}
	
	public void unregister() {
		CreatureSpawnEvent.getHandlerList().unregister(this);
		PlayerInteractEntityEvent.getHandlerList().unregister(this);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onCreatureSpawnEvent(CreatureSpawnEvent event){
		String w = event.getLocation().getWorld().getName();
		if (w.equalsIgnoreCase("Dungeons") || w.equalsIgnoreCase("Questing") || w.equalsIgnoreCase("Dratan")) {
			if (!event.getEntityType().equals(EntityType.HORSE)) {
				event.setCancelled(true);
				return;
			}
		}
		if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) {
//			RandomSpawningHandler.entitylist.add(event.getEntity());
//			RandomSpawningHandler.Spawn(event.getEntity());
			event.setCancelled(true);
			return;
		}
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING 
				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG
				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
			boolean cancel = false;
			if (individualEntityCap && allowedEntities.contains(event.getEntityType())) {
				cancel = checkAndApplyEntityWithCap(event.getEntityType(), 
						event.getLocation().getChunk(),
						individualCapData.get(event.getEntityType()));
			} else {
				cancel = checkAndApplyEntity(event.getEntityType(), 
						event.getLocation().getChunk());
			}
			if (cancel) {
				event.setCancelled(true);
				return;
			}
		}
		if ((w.equalsIgnoreCase("Dungeons") || w.equalsIgnoreCase("RPG"))
				&& event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
			event.setCancelled(true);
			return;
		}
	}
	
	public boolean checkAndApplyEntityWithCap(EntityType ent, Chunk chunk, Integer cap)
	{
		/*
		 * Checks and applies entity cap using individual cap limits
		 */
		Entity[] entitiesInEventChunk = chunk.getEntities();
		
		Integer entcount = 0;
		for (Entity entt : entitiesInEventChunk)
		{
			//we have entity list, so iterate through it
			//to determine just how many of the same type
			//we have on the same chunk
			if (entt.getType() == ent)
			{
				//same type detected, so increase
				entcount++;
			}
		}
		
		if (entcount >= cap)
		{
			return true;
		} 
		else
		{
			return false;
		}
	}
	
	public boolean checkAndApplyEntity(EntityType ent, Chunk chunk)
	{
		/*
		 * Checks and applies entity cap using global cap limit
		 */
		Entity[] entitiesInEventChunk = chunk.getEntities();
		
		int entcount = 0;
		for (Entity entt : entitiesInEventChunk)
		{
			//we have entity list, so iterate through it
			//to determine just how many of the same type
			//we have on the same chunk
			if (entt.getType() == ent)
			{
				//same type detected, so increase
				entcount++;
			}
		}
		
		if (entcount >= spawnCap)
		{
			return true;
		} 
		else
		{
			return false;
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event)
	{
		Player p = event.getPlayer();
		
		if ((p.getItemInHand().getType() == Material.WHEAT ||
				p.getItemInHand().getType() == Material.CARROT ||
						p.getItemInHand().getType() == Material.SEEDS ||
				p.getItemInHand().getType() == Material.RAW_BEEF ||
				p.getItemInHand().getType() == Material.PORK ||
				p.getItemInHand().getType() == Material.COOKED_BEEF ||
				p.getItemInHand().getType() == Material.RAW_FISH)
				&& allowedEntities.contains(event.getRightClicked().getType()))
		{
			boolean cancel = false;
			if (individualEntityCap) {
				cancel = checkAndApplyEntityWithCap(
						event.getRightClicked().getType(), 
						event.getRightClicked().getLocation().getChunk(), 
						individualCapData.get(event.getRightClicked().getType()));
			} else {
				int entcount = 0;
				//player is holding wheat and the entity is allowed, check
				for (Entity ent : event.getRightClicked().getLocation().getChunk().getEntities())
				{
					if (allowedEntities.contains(ent.getType()))
						entcount++;
				}
				cancel = entcount >= spawnCap;
				
				
			}
			if (cancel) {
				p.sendMessage(WAddon.lang.get("Breeding"));
				event.setCancelled(true);
			}
		}
		
	}
}


