package me.wiedzmin137.waddon.database;

import java.util.HashMap;
import java.util.Map;

import me.wiedzmin137.waddon.WAddon;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.api.events.HeroKillCharacterEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class TownData implements Listener {
	@SuppressWarnings("unused")
	private WAddon plugin;
	private Town town;
	
	private int kingdomPoints;
	private Level townLevel;
	
	protected static Map<Town, TownData> towns = new HashMap<Town, TownData>();

	public TownData(WAddon plugin, Town town) {
		this.plugin = plugin;
		this.town = town;
		towns.put(town, this);
	}
	
	public void upgradeTown(CommandSender sender) { 
		//ifs
		if (sender.hasPermission("waddon.upgrade")) {
			if (Level.getNext(townLevel).getPointsNeeded() <= kingdomPoints) {
				townLevel = Level.getNext(townLevel);
				kingdomPoints -= townLevel.getPointsNeeded();
			}
		}
	}
	
	@EventHandler
	public static void onDeath(HeroKillCharacterEvent event) {
		if (event.getDefender() instanceof Hero) {
			try {
				Resident attack = TownyUniverse.getDataSource().getResident(event.getAttacker().getName());
				if (!attack.getTown().getNation().isNeutral()) {
					Resident defend = TownyUniverse.getDataSource().getResident(((Hero) event.getDefender()).getName());
					for (Nation nation : attack.getTown().getNation().getEnemies()) {
						if (nation.equals(defend.getTown().getNation())) {
							getTownData(attack.getTown()).kingdomPoints++;
							TownyUniverse.getPlayer(attack).sendMessage("You have got 1 KingdomPoint for killing player");
							break;
						}
					}
				}
			} catch (TownyException e) {}
		}
	}
	
	public void setKingdomPoints(int points) { 
		kingdomPoints = points; 
		//plugin.getDataManager().saveTown(this);
	}
	
	public Town getTown() { return town; }
	public Level getTownLevel() { return townLevel; }
	public int getKingdomPoints() { return kingdomPoints; }
	public static TownData getTownData(Town town) { return towns.get(town); }
	
	public static enum Level {
		FIRST(1, 100),
		SECOND(2, 150),
		THIRD(3, 200),
		FOURTH(4, 250),
		FIFTH(5, 300),
		SIXTH(6, 350),
		SEVENTH(7, 400),
		EIGHTH(8, 450),
		NINETH(9, 500),
		TENTH(10, 550);
		
		private int intLevel;
		private int pointsNeeded;
		
		private Level(int intLevel, int pointsNeeded) {
			this.intLevel = intLevel;
			this.pointsNeeded = pointsNeeded;
		}
		
		public int getPointsNeeded() {
			return pointsNeeded;
		}
		
		public int toInt() {
			return intLevel;
		}
		
		public static Level getNext(Level level) {
			switch (level) {
			case FIRST: return SECOND;
			case SECOND: return THIRD;
			case THIRD: return FOURTH;
			case FOURTH: return FIFTH;
			case FIFTH: return SIXTH;
			case SIXTH: return SEVENTH;
			case SEVENTH: return EIGHTH;
			case EIGHTH: return NINETH;
			case NINETH: return TENTH;
			default: return null;
			}
		}
	}
}
