package me.wiedzmin137.waddon.database;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class WPlayerData implements Listener {
	public static HashMap<String, WPlayerData> players = new HashMap<>();
	
	private int stamina = 50;
	private int brokenLegTime = 0;
	
	private Player player;
	private Race race;
	
	public int STRENGTH;
	public int DEXTERITY;
	public int DURABILITY;
	public int INTELLIGENCE;
	public int WISDOM;
	
	public WPlayerData(Player player, Race race) {
		this.player = player;
		this.race = race;
		players.put(player.getName(), this);
		
		STRENGTH = Attribute.STRENGTH.getVal(race);
		DEXTERITY = Attribute.DEXTERITY.getVal(race);
		DURABILITY = Attribute.DURABILITY.getVal(race);
		INTELLIGENCE = Attribute.INTELLIGENCE.getVal(race);
		WISDOM = Attribute.WISDOM.getVal(race);
	}
	
	public void breakLeg() {
		brokenLegTime = 180;
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
	}
	public void healBrokenLeg() {
		player.removePotionEffect(PotionEffectType.SLOW);
		brokenLegTime = 0;
	}
	
	public void recalcStats(Race race) {
		STRENGTH = Attribute.STRENGTH.getVal(race);
		DEXTERITY = Attribute.DEXTERITY.getVal(race);
		DURABILITY = Attribute.DURABILITY.getVal(race);
		INTELLIGENCE = Attribute.INTELLIGENCE.getVal(race);
		WISDOM = Attribute.WISDOM.getVal(race);
	}
	
	public void setRace(Race race) { this.race = race; Race.names.put(player.getName(), race); recalcStats(race); }
	public void setStamina(int stamina) { this.stamina = stamina; }
	public void setBrokenLegTime(int time) { this.brokenLegTime = time; }
	
	public int getStamina() { return stamina; }
	public int getBrokenLegTime() { return brokenLegTime; }
	public Race getRace() { return race; }
	public Player getPlayer() { return player; }
}
