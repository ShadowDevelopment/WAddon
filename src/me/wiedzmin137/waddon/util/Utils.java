package me.wiedzmin137.waddon.util;

import java.util.Random;

import me.wiedzmin137.waddon.database.WPlayerData;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class Utils {
	public static String u(String str) {
		return me.wiedzmin137.wheroesaddon.util.Utils.u(str);
	}
	
	public static int getRandom(int min, int max) {
		return new Random().nextInt((max - min) + 1) + min;
	}
	
	public static void takeOne(Player p, ItemStack i){
		if (i.getAmount() <= 1){
			p.getInventory().removeItem(i);
		}
		if (i.getAmount() > 1){
			i.setAmount(i.getAmount() - 1);
		}
	}
	
//TODO Change to HD
//	public static void expMessage(Player p, Location loc, double gained, double needed, double current) {
//		if (gained == 0) { return; }
//		HoloAPI.getManager().createSimpleHologram(loc, 150,
//				"&a&l+ &2&o" + gained + WAddon.lang.get("XP"),
//				"&2[ &9" + current + "&1/&9" + needed + " &2]");
//	}
	
	public static boolean magicCritAttack(Hero hero) {
		int chance = WPlayerData.players.get(hero.getPlayer().getName()).INTELLIGENCE - 10;
		if (chance < 0) return false;
		
		Random generator = new Random();
		int roll = generator.nextInt(100) + 1;
		if (chance >= roll) {
			//WAddon.getInstance().getServer().getLogger().info("[WAddon] + CH:" + chance + " ROLL:" + roll);
			return true;
		}
		//WAddon.getInstance().getServer().getLogger().info("[WAddon] CH:" + chance + " ROLL:" + roll);
		return false;
	}
	
	public static int getHeroesMana(Hero hero) {
		int level = hero.getLevel();
		int basemana = hero.getHeroClass().getBaseMaxMana();
		double basemanaperlevel = hero.getHeroClass().getMaxManaPerLevel();
		
		int basemanaNew = (int)basemanaperlevel * level;
		int mana = basemana + basemanaNew;
		
		return mana;
	}
	
	public static void applyManaBonus(Hero hero) {
		Player player = hero.getPlayer();
		if (!player.isValid()) {
			return;
		}
		int madrosc = (WPlayerData
				.players
				.get(player.getName())
				.WISDOM - 10) * 2;
		if ((hero instanceof Hero)) {
			if (hero.getMaxMana() < getHeroesMana(hero) + madrosc) {
				if (madrosc > 0) {
					hero.addMaxMana(hero.getName().toString(), madrosc);
				}
				return;
			}
			if (hero.getMaxMana() > getHeroesMana(hero) + madrosc) {
				if (madrosc <= 0) {
					hero.removeMaxMana(hero.getName().toString());
				}
				return;
			}
		}
	}
	
	public static boolean isResident(Player player, Location location) {
		try {
			return TownyUniverse.getTownBlock(location).getTown().hasResident(player.getName());
		}
		catch (NotRegisteredException ex) {}
		return false;
	}
	
	public static boolean isResident(Player player, Location... locations) {
		for (Location location : locations) {
			if (!isResident(player, location)) {
				return false;
			}
		}
		return true;
	}
	  
	public static boolean isPlotOwner(Player player, Location location) {
		try {
			TownBlockOwner owner = TownyUniverse.getDataSource().getResident(player.getName());
			return TownyUniverse.getTownBlock(location).isOwner(owner);
		}
		catch (NotRegisteredException ex) {}
		return false;
	}
	  
	public static boolean isPlotOwner(Player player, Location... locations) {
		for (Location location : locations) {
			if (!isPlotOwner(player, location)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isInWilderness(Location location) {
		return TownyUniverse.isWilderness(location.getBlock());
	}
	
	public static boolean isInWilderness(Location... locations) {
		for (Location location : locations) {
			if ((location != null) && (!isInWilderness(location))) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isInsideShopPlot(Location location) {
		return TownyUniverse.getTownBlock(location).getType() == TownBlockType.COMMERCIAL;
	}
	
	public static boolean isInsideShopPlot(Location... locations) {
		for (Location location : locations) {
			if ((location != null) && (!isInsideShopPlot(location))) {
				return false;
			}
		}
		return true;
	}
	
	public static String getFancyDescription(String oldDesc, Hero hero, Skill skill) {
		int cooldown = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN.node(), 0, false)
				- SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(skill)) / 1000;
		if (cooldown > 0) {
			oldDesc += " CD:" + cooldown + "s";
		}
		
		int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA.node(), 10, false)
				- (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(skill));
		if (mana > 0) {
			oldDesc += " M:" + mana;
		}
		
		int healthCost = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.HEALTH_COST, 0, false) - 
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(skill));
		if (healthCost > 0) {
			oldDesc += " HP:" + healthCost;
		}
		
		int staminaCost = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.STAMINA.node(), 0, false)
				- (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(skill));
		if (staminaCost > 0) {
			oldDesc += " FP:" + staminaCost;
		}
		
		int delay = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DELAY.node(), 0, false) / 1000;
		if (delay > 0) {
			oldDesc += " W:" + delay + "s";
		}
		
		int exp = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.EXP.node(), 0, false);
		if (exp > 0) {
			oldDesc += " XP:" + exp;
		}
		return oldDesc;
	}
}
