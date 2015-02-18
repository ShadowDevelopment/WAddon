package me.wiedzmin137.waddon.database;

import java.io.File;
import java.sql.SQLException;

import me.wiedzmin137.waddon.WAddon;
import me.wiedzmin137.waddon.util.Utils;
import me.wiedzmin137.wheroesaddon.WHeroesAddon;
import me.wiedzmin137.wheroesaddon.util.database.Database;
import me.wiedzmin137.wheroesaddon.util.database.DatabaseConfigBuilder;
import me.wiedzmin137.wheroesaddon.util.database.Table;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import ca.wacos.nametagedit.NametagAPI;

import com.herocraftonline.heroes.characters.Hero;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class DataManager implements Listener {
	private WAddon plugin;

	public static final Table KINGDOM_POINTS = new Table("KINGDOM_POINTS", "NAME VARCHAR(16),kingdom_points INT");
	public static final Table REAL_FIGHT = new Table("REAL_FIGHT", "uuid VARCHAR(36),stamina INT,brokenLeg INT");
	public static final Table STATISTICS = new Table("STATISTICS", "uuid VARCHAR(36),race VARCHAR(8),STRENGTH INT,DEXTERITY INT,DURABILITY INT,INTELLIGENCE INT,WISDOM INT");
	
	private String SMage = Utils.u(" §r[§6&#10031;§r]");
	private String SKnight = Utils.u(" §r[§6&#x26e8;§r]");
	private String SRogue = Utils.u(" §r[§6&#x2694;§r]");
	private String SRanger = Utils.u(" §r[§6&#10166;§r]");
	//private static String bonus = Utils.u("&2&oYou have this server in your list. Bonus&r: &a+&r&o5% XP");

	private Database database;
	
	public DataManager(WAddon WAddon) {
		this.plugin = WAddon;
		WHeroesAddon wHeroesAddon = WHeroesAddon.getInstance();
		
		File sqliteFile = new File(wHeroesAddon.getDataFolder(), "kingdom.db");
		DatabaseConfigBuilder config = new DatabaseConfigBuilder(sqliteFile);
		database = getDatabasee(config);
		
		try {
			database.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		database.registerTable(KINGDOM_POINTS);
		database.registerTable(REAL_FIGHT);
		database.registerTable(STATISTICS);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		Town town = null;
		try {
			town = TownyUniverse.getDataSource().getResident(event.getPlayer().getName()).getTown();
			TownData.getTownData(town);
		} catch (NullPointerException e) {
			TownData td = new TownData(plugin, town);
			try {
				td.setKingdomPoints((Integer) database.get(KINGDOM_POINTS, "NAME", "kingdom_points", town.getName().toLowerCase()));
			} catch (NullPointerException e2) {
				td.setKingdomPoints(0);
			}
			if (database.contains(KINGDOM_POINTS, "NAME", td.getTown().getName().toLowerCase())) {
				database.update(KINGDOM_POINTS, "NAME", "kingdom_points", td.getTown().getName().toLowerCase(), td.getKingdomPoints());
			} else {
				database.set(KINGDOM_POINTS, td.getTown().getName().toLowerCase(), td.getKingdomPoints());
			}
		} catch (TownyException ex) {
		}
		
		loadPlayer(event.getPlayer());
		
//		if (WAddon.heldAddresses.containsKey(player.getAddress().getAddress())) {
//			WAddon.heldAddresses.remove(player.getAddress().getAddress() );
//			WAddon.playersJoinedWith.add(player.getUniqueId() );
//			player.sendMessage("");
//			player.sendMessage(bonus);
//		}
		
		final WPlayerData wp = WPlayerData.players.get(player.getName());
		final Hero hero = WAddon.heroes.getCharacterManager().getHero(player);
		hero.setVerbose(false);
		Utils.applyManaBonus(hero);
		player.setHealthScale(Math.floor(hero.resolveMaxHealth() * 100) / 100);
		
//		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
//			@Override
//			public void run() {
//				if (player.getOpenInventory() == null) {
//					if (Race.names.get(player.getName()) == null) {
//						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setRace " + player.getName());
//					}
//				}
//			}
//		}, 10);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				String suffix;
				switch (hero.getHeroClass().getName()) {
					case "Mage": suffix = SMage; break;
					case "Knight": suffix = SKnight; break;
					case "Rogue": suffix = SRogue; break;
					case "Ranger": suffix = SRanger; break;
					default: suffix = "";
				}
				NametagAPI.setPrefix(player.getName(), wp.getRace().getDisplayName());
				NametagAPI.setSuffix(player.getName(), suffix);
			}
		}, 10);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerKickEvent event) {
		Town town = null;
		try {
			town = TownyUniverse.getDataSource().getResident(event.getPlayer().getName()).getTown();
			if (TownyUniverse.getOnlinePlayers(town).isEmpty() || TownyUniverse.getOnlinePlayers(town).toArray().length == 1) {
				TownData td = TownData.getTownData(town);
				if (database.contains(KINGDOM_POINTS, "NAME", td.getTown().getName().toLowerCase())) {
					database.update(KINGDOM_POINTS, "NAME", "kingdom_points", td.getTown().getName().toLowerCase(), td.getKingdomPoints());
				} else {
					database.set(KINGDOM_POINTS, td.getTown().getName().toLowerCase(), td.getKingdomPoints());
				}
				TownData.towns.put(town, null);
			}
		} catch (TownyException e) {
		}
		
		
	}
	
	@EventHandler
	public void onPluginLoad(PluginEnableEvent event) {
		if (event.getPlugin().equals(plugin)) {
			for (Town town : TownyUniverse.getDataSource().getTowns()) {
				TownData td = new TownData(plugin, town);
				
				try {
					td.setKingdomPoints((Integer) database.get(KINGDOM_POINTS, "NAME", "kingdom_points", town.getName().toLowerCase()));
				} catch (NullPointerException e) {
					td.setKingdomPoints(0);
				}
			}
		}
	}
	
	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		if (event.getPlugin().equals(plugin)) {
			for (Town town : TownyUniverse.getDataSource().getTowns()) {
				TownData td = TownData.getTownData(town);
				if (database.contains(KINGDOM_POINTS, "NAME", td.getTown().getName().toLowerCase())) {
					database.update(KINGDOM_POINTS, "NAME", "kingdom_points", td.getTown().getName().toLowerCase(), td.getKingdomPoints());
				} else {
					database.set(KINGDOM_POINTS, td.getTown().getName().toLowerCase(), td.getKingdomPoints());
				}
			}
		}
	}
	
	public WPlayerData loadPlayer(Player player) {
		checkConnection();
		
		Race race = Race.HUMAN;
		try {
			if (database.contains(STATISTICS, "uuid", player.getUniqueId())) {
				Race raceTemp = Race.valueOf(String.valueOf(database.get(STATISTICS, "uuid", "race", player.getUniqueId())));
				if (raceTemp != null) {
					race = raceTemp;
					Race.names.put(player.getName(), race);
				}

			}
		} catch (NullPointerException e) {}
		
		WPlayerData sd = new WPlayerData(player, race);
		try {
			if (database.contains(REAL_FIGHT, "uuid", player.getUniqueId())) {
				sd.setStamina((Integer) database.get(REAL_FIGHT, "uuid", "stamina", player.getUniqueId()));
			} else {
				sd.setStamina(50);
			}
		} catch (NullPointerException e) {
			sd.setStamina(50);
		}
		
		try {
			if (database.contains(REAL_FIGHT, "uuid", player.getUniqueId())) {
				sd.setBrokenLegTime((Integer) database.get(REAL_FIGHT, "uuid", "brokenLeg", player.getUniqueId()));
			} else {
				sd.setBrokenLegTime(0);
			}
		} catch (NullPointerException e) {
			sd.setBrokenLegTime(0);
		}
		
		try {
			if (database.contains(STATISTICS, "uuid", player.getUniqueId())) {
				sd.STRENGTH = (Integer) database.get(STATISTICS, "uuid", "STRENGTH", player.getUniqueId());
				sd.DEXTERITY = (Integer) database.get(STATISTICS, "uuid", "DEXTERITY", player.getUniqueId());
				sd.DURABILITY = (Integer) database.get(STATISTICS, "uuid", "DURABILITY", player.getUniqueId());
				sd.INTELLIGENCE = (Integer) database.get(STATISTICS, "uuid", "INTELLIGENCE", player.getUniqueId());
				sd.WISDOM = (Integer) database.get(STATISTICS, "uuid", "WISDOM", player.getUniqueId());
			}
		} catch (NullPointerException e) {}
		savePlayer(sd);
		return sd;
	}
	
	public void savePlayer(WPlayerData wp) {
		checkConnection();
		if (database.contains(REAL_FIGHT, "uuid", wp.getPlayer().getUniqueId())) {
			database.update(REAL_FIGHT, "uuid", "stamina", wp.getPlayer().getUniqueId(), wp.getStamina());
			database.update(REAL_FIGHT, "uuid", "brokenLeg", wp.getPlayer().getUniqueId(), wp.getBrokenLegTime());
		} else {
			database.set(REAL_FIGHT, wp.getPlayer().getUniqueId(), wp.getStamina(), wp.getBrokenLegTime());
		}
		if (database.contains(STATISTICS, "uuid", wp.getPlayer().getUniqueId())) {
			database.update(STATISTICS, "uuid", "race", wp.getPlayer().getUniqueId(), wp.getRace().toString());
			database.update(STATISTICS, "uuid", "STRENGTH", wp.getPlayer().getUniqueId(), wp.STRENGTH);
			database.update(STATISTICS, "uuid", "DEXTERITY", wp.getPlayer().getUniqueId(), wp.DEXTERITY);
			database.update(STATISTICS, "uuid", "DURABILITY", wp.getPlayer().getUniqueId(), wp.DURABILITY);
			database.update(STATISTICS, "uuid", "INTELLIGENCE", wp.getPlayer().getUniqueId(), wp.INTELLIGENCE);
			database.update(STATISTICS, "uuid", "WISDOM", wp.getPlayer().getUniqueId(), wp.WISDOM);
		} else {
			database.set(STATISTICS, wp.getPlayer().getUniqueId(), wp.getRace().toString(), wp.STRENGTH, wp.DEXTERITY, wp.DURABILITY, wp.INTELLIGENCE, wp.WISDOM);
		}
	}
	
	private void checkConnection() {
		if (!database.isConnected()) {
			try {
				database.connect();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Database getDatabasee(DatabaseConfigBuilder builder) {
		return new Database(WHeroesAddon.getInstance(), builder);
	}
	
	public Database getDatabase() { return database; }
	protected void disconnect() { database.close(); }
}
