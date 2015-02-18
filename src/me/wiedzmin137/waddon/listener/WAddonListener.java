package me.wiedzmin137.waddon.listener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.wiedzmin137.waddon.BarAPI;
import me.wiedzmin137.waddon.WAddon;
import me.wiedzmin137.waddon.database.Race;
import me.wiedzmin137.waddon.database.WPlayerData;
import me.wiedzmin137.waddon.util.Utils;
import me.wiedzmin137.wheroesaddon.util.FirstClassChooseEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.Acrobot.ChestShop.Events.Protection.BuildPermissionEvent;
import com.herocraftonline.heroes.api.events.ClassChangeEvent;
import com.herocraftonline.heroes.api.events.ExperienceChangeEvent;
import com.herocraftonline.heroes.api.events.HeroChangeLevelEvent;
import com.herocraftonline.heroes.api.events.ManaChangeEvent;
import com.herocraftonline.heroes.api.events.SkillDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import com.herocraftonline.heroes.util.Util;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class WAddonListener implements Listener {
	private WAddon plugin;
	
	private static HashMap<String, BukkitTask> manaQueue = new HashMap<>();
	
	public WAddonListener(WAddon plugin) {
		this.plugin = plugin;
	}
	
	private static List<String> itemsToCheck = new ArrayList<>();
	static {
		itemsToCheck.addAll(Util.axes);
		itemsToCheck.addAll(Util.swords);
	}
	
	@EventHandler
	public void onServerListPing(ServerListPingEvent e) {
		WAddon.heldAddresses.put(e.getAddress(), (long) (5 * 60));
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		WAddon.heroes.getCharacterManager().getHero(event.getEntity()).setVerbose(false);
	}
	
	@EventHandler
	public void onLevel(HeroChangeLevelEvent event) {
		event.getHero().setVerbose(false);
	}
	
	@EventHandler
	public void onClassChange(ClassChangeEvent event) {
		event.getHero().setVerbose(false);
		if (Race.names.get(event.getHero().getPlayer().getName()) == null) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setRace " + event.getHero().getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onFirstClassChoose(FirstClassChooseEvent event) {
		if (Race.names.get(event.getHero().getPlayer().getName()) == null) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setRace " + event.getHero().getPlayer().getName());
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "itemsgive " + event.getHero().getPlayer().getName() + " " + event.getTo().getName());
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Hero hero = WAddon.heroes.getCharacterManager().getHero(event.getPlayer());
		event.getPlayer().setHealth(5.0D);
		Utils.applyManaBonus(hero);
	}
	  
	@EventHandler(priority=EventPriority.MONITOR)
	public void onItemBreak(PlayerItemBreakEvent event) {
		Hero hero = WAddon.heroes.getCharacterManager().getHero(event.getPlayer());
		Utils.applyManaBonus(hero);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent event) {
		Hero hero = WAddon.heroes.getCharacterManager().getHero((Player)event.getPlayer());
		Utils.applyManaBonus(hero);
	}
	
	@EventHandler
	public void onExpGain(ExperienceChangeEvent event) {
		if (event.getExpChange() > 0) {
			if (event.getSource().equals(ExperienceType.KILLING)) {
				if (WAddon.playersJoinedWith.contains(event.getHero().getPlayer().getUniqueId())) {
					event.setExpGain(event.getExpChange() * 1.05);
				}
			}
			NumberFormat formatter = new DecimalFormat("#0.0");
			event.getHero().getPlayer().sendMessage(Utils.u("&2&lXP gained: &r&o" + formatter.format(event.getExpChange())));
		}
	}
	
	@EventHandler
	public void onHunger(FoodLevelChangeEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			Player player = (Player) event.getEntity();
				event.setCancelled(true);
				player.setFoodLevel(WPlayerData.players.get(player.getName()).getStamina() / 5);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (player.getLocation().getWorld().getName().equalsIgnoreCase("RPG")) {
			try {
				Resident res = TownyUniverse.getDataSource().getResident(player.getName());
				if (!res.hasTown()) {
					event.setRespawnLocation(Bukkit.getWorld("Spawn").getSpawnLocation());
					player.teleport(Bukkit.getWorld("Spawn").getSpawnLocation());
					return;
				} else {
					try {
						event.setRespawnLocation(res.getTown().getSpawn());
						player.teleport(res.getTown().getSpawn());
					} catch (TownyException e) {
						event.setRespawnLocation(Bukkit.getWorld("Spawn").getSpawnLocation());
						player.teleport(Bukkit.getWorld("Spawn").getSpawnLocation());
					}
				}
			} catch (NotRegisteredException e) {}
		}
	}
	
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {	 
		Player sender = event.getPlayer();
		if (event.getMessage().equalsIgnoreCase("/hero")) {
			event.setCancelled(true);
			Bukkit.dispatchCommand(sender, "showsms Help " + sender.getName());
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Race.names.remove(event.getPlayer());
	}
	
	@EventHandler
	public static void onBuild(BuildPermissionEvent event) {
		final Location chest = event.getChest();
		final Location sign = event.getSign();
		if (event.getPlayer().hasPermission("ChestShop.towny.bypass")) {
			event.allow();
		}
		if (Utils.isInWilderness(chest, sign) || !Utils.isInsideShopPlot(chest, sign)) {
			event.disallow();
			return;
		}
		boolean allow = (Utils.isPlotOwner(event.getPlayer(), chest, sign) || Utils.isResident(event.getPlayer(), chest, sign));
		event.allow(allow);
	}
	
	@EventHandler
	public void onConsume(PlayerItemConsumeEvent e) {
		final Player player = e.getPlayer(); 
		final ItemStack item = e.getItem();
		if (item.getType() == Material.POTION) {
			boolean isManaPotion = false;
			int manaAmount = 0;
			for (String lore : item.getItemMeta().getLore()) {
				lore = ChatColor.stripColor(lore);
				boolean isManaVariable = false;
				for (String subLore : lore.split("[: ]")) {
					if (isManaVariable == false) {
						if (subLore.equalsIgnoreCase("Mana")) {
							isManaVariable = true;
						}
					} else {
						try {
							Integer test = Integer.parseInt(subLore);
							manaAmount = test;
							isManaPotion = true;
						} catch (NumberFormatException ne) {
							continue;
						}
					}
				}
			}
			if (isManaPotion) {
				Hero hero = WAddon.heroes.getCharacterManager().getHero(player);
				if (manaAmount + hero.getMana() > hero.getMaxMana()) {
					hero.setMana(hero.getMaxMana());
				} else {
					hero.setMana(hero.getMana() + manaAmount);
				}
			}
		}
	}
	
	@EventHandler
	public void onManaChange(ManaChangeEvent event) {
		event.getHero().setVerbose(false);
		final Player p = event.getHero().getPlayer();
		if (BarAPI.hasBar(p)) {
			BarAPI.removeBar(p);
		}
		if (manaQueue.get(p.getName()) == null) {
			manaQueue.put(p.getName(), new BukkitRunnable() {
				@Override
				public void run() {
					manaQueue.remove(p.getName());
					BarAPI.removeBar(p);
				}
			}.runTaskLater(plugin, 100));
		} else {
			manaQueue.get(p.getName()).cancel();
			manaQueue.put(p.getName(), new BukkitRunnable() {
				@Override
				public void run() {
					manaQueue.remove(p.getName());
					BarAPI.removeBar(p);
				}
			}.runTaskLater(plugin, 100L));
		}
		
		BarAPI.setMessage(p, Utils.u("&3&o&lMana: &3&o" + event.getFinalMana()), (float) event.getFinalMana() / event.getHero().getMaxMana() * 100);
	}
	
	@EventHandler
	public void onCraftItem(PrepareItemCraftEvent e) {
		Material itemType = e.getRecipe().getResult().getType();
		//Byte itemData = e.getRecipe().getResult().getData().getData();
		if (itemType == Material.ENDER_CHEST || itemType == Material.ANVIL || itemType == Material.TNT || itemType == Material.BEACON) {
			e.getInventory().setResult(new ItemStack(Material.AIR));
			for (HumanEntity he : e.getViewers()) {
				if (he instanceof Player) {
					((Player) he).sendMessage(ChatColor.RED + "You cannot craft this!");
				}
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			if (event.getCause() == DamageCause.STARVATION) {
				event.setCancelled(true);
				return;
			} else if (event.getCause() == DamageCause.FALL) {
				WPlayerData sd = WPlayerData.players.get(((Player) event.getEntity()).getName());
				double damage = event.getDamage();
				if (sd.getPlayer().getHealth() - damage <= 0.0D) {
					return;
				}
				if (sd.getBrokenLegTime() == 0) {
					event.setDamage(damage / 1.5);
					if (damage >= 15) {
						sd.getPlayer().sendMessage(Utils.u(WAddon.lang.get("Diseases.BrokenLeg")));
						sd.breakLeg();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onDamageFight(EntityDamageByEntityEvent event) {
		double finalDMG = event.getDamage();
		if (finalDMG == 0.0D) {
			return;
		}
		Entity attacker  = event.getDamager();
		Entity victim = event.getEntity();
		if (attacker instanceof Player) {
			if (finalDMG < 1) {
				event.setCancelled(true);
				if (victim instanceof Player) {
					((Player) victim).sendMessage(ChatColor.RED + "You are too tired to attack!");
					event.setCancelled(true);
					return;
				}
			}
			if (victim instanceof Player) {
				Player damager = (Player) attacker;
				
				Material material = ((Player) attacker).getEquipment().getItemInHand().getType();
				boolean type = false;
				for (String string : itemsToCheck) {
					if (string.equalsIgnoreCase(material.name())) {
						type = true;
						break;
					}
				}
					
				boolean isBlocking = ((Player) victim).isBlocking();
				Player victimP = (Player) victim;
				WPlayerData sdDam = WPlayerData.players.get(damager.getName());
				WPlayerData sdVic = WPlayerData.players.get(victimP.getName());
				int staminaDam = sdDam.getStamina();
				int staminaVic = sdVic.getStamina();
				if (type == true) {
					int dodgeChange = Utils.getRandom(0, 20) + sdDam.DEXTERITY / 10; //TODO upgrade Chance by Dexterity
					if (dodgeChange == 20) {
						event.setCancelled(true);
						victimP.getWorld().playEffect(victimP.getLocation(), Effect.SMOKE, 1);
						victimP.sendMessage(ChatColor.RED + "Opponent dodged!");
						damager.sendMessage(ChatColor.DARK_GREEN + "Dodge!");
						return;
					}
				}
				if (isBlocking) {
					if (material == null || material.equals(Material.AIR)) {
						if (staminaDam > 50) {
							damager.sendMessage(ChatColor.DARK_GREEN + "You are too tired to attack him using hands!");
							event.setCancelled(true);
							return;
						} else {
							sdDam.setStamina(staminaDam - (Utils.getRandom(5, 10) - sdDam.DURABILITY / 5));
							event.setDamage(finalDMG + sdDam.DEXTERITY / 5); 
							return;
						}
					}
					int blockFail = Utils.getRandom(0, 5);
					if (staminaVic > 20) {
						if (blockFail < 5) {
							sdVic.setStamina(staminaVic - (Utils.getRandom(5, 10) - sdDam.DURABILITY / 5));
							victimP.getWorld().playEffect(victimP.getLocation(), Effect.BLAZE_SHOOT, 1);
							if (material.equals(Material.WOOD_SWORD)) {
								victimP.getWorld().playSound(victimP.getLocation(), Sound.ZOMBIE_WOODBREAK, 0.5F, Utils.getRandom(1, 2));
							} else {
								victimP.getWorld().playSound(victimP.getLocation(), Sound.ANVIL_LAND, 0.5F, Utils.getRandom(1, 2));
							}
							victimP.sendMessage(ChatColor.RED + "Block!");
							damager.sendMessage(ChatColor.DARK_GREEN + "Opponent blocked your attack!");
							event.setCancelled(true);
							return;
						} else {
							if (material.equals(Material.BOW)) {
								event.setDamage(finalDMG + sdDam.DEXTERITY / 5); 
							} else {
								event.setDamage(finalDMG + sdDam.STRENGTH / 5);		
							}
						}
					} else {
						victimP.sendMessage(ChatColor.RED + "You did not block the blow!");
						damager.sendMessage(ChatColor.DARK_GREEN + "You were able to break the block opponent!");
						return;
					}
				} else {
					if (staminaDam > 20) {
						sdDam.setStamina(staminaDam - (Utils.getRandom(5, 10) - sdDam.DURABILITY / 5));
						if (material.equals(Material.BOW)) {
							event.setDamage(finalDMG + sdDam.DEXTERITY / 5); 
						} else {
							event.setDamage(finalDMG + sdDam.STRENGTH / 5);		
						}
						return;
					} else {
						event.setCancelled(true);
						damager.sendMessage(ChatColor.RED + "You are too tired to attack!");
						victimP.sendMessage(ChatColor.DARK_GREEN + "Your opponent is too tired to attack!");
						return;
					}
				}
			}
		} else {
			if (victim instanceof Player) {
				Player vic = (Player) victim;
				WPlayerData sd = WPlayerData.players.get(vic.getName());
				if (attacker instanceof Arrow) {
					if (sd.getStamina() > 20) {
						sd.setStamina(sd.getStamina() - (Utils.getRandom(10, 20) - sd.DURABILITY / 3));
						event.setDamage(finalDMG + sd.DEXTERITY / 5); //TODO upgrade DMG by Dexterity
						return;
					}
				} else if ((attacker instanceof Skeleton
						|| attacker instanceof Zombie
						|| attacker instanceof Spider)) {
					if (WPlayerData.players.get(vic.getName()).getStamina() > 20) {
						if (vic.isBlocking()) {
							if (Utils.getRandom(1, 5) < 5) {
								sd.setStamina(sd.getStamina() - (Utils.getRandom(5, 10) - sd.DURABILITY / 5));
								event.setCancelled(true);
								vic.getWorld().playEffect(vic.getLocation(), Effect.BLAZE_SHOOT, 1);
								if (vic.getEquipment().getItemInHand().getType().equals(Material.WOOD_SWORD)) {
									vic.getWorld().playSound(vic.getLocation(), Sound.ZOMBIE_WOODBREAK, 0.5F, Utils.getRandom(1, 2));
								} else {
									vic.getWorld().playSound(vic.getLocation(), Sound.ANVIL_LAND, 0.5F, Utils.getRandom(1, 2));
								}
								return;
							} 
						} 
					} else {
						vic.sendMessage(ChatColor.RED + "You are to tired to block attacks!");
						return;
					}
				}
			} else {
				if (attacker instanceof Arrow && ((Arrow) attacker).getShooter() instanceof Player) {
					if (victim instanceof Monster) {
						((Monster) victim).setTarget((LivingEntity) ((Arrow) attacker).getShooter());
					}
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onSpellDamageEvent(SkillDamageEvent event) {
		if ((event.getDamager() instanceof Hero)) {

			//WAddon.LOG.info("SkillDamageEvent " + event.getEntity().toString());
			
			WPlayerData wp = WPlayerData.players.get(event.getDamager().getName());
			if (wp.INTELLIGENCE < 10) return;
			
			double modifieddmg = event.getDamage() / 100.0D * 5 * (wp.INTELLIGENCE - 9);
			int crit = 1;
			if (Utils.magicCritAttack((Hero)event.getDamager())) {
				((Hero)event.getDamager()).getPlayer().sendMessage(Utils.u(WAddon.lang.get("SpellCritic")));
				crit = 2;
			}
			
			//WAddon.LOG.info("" + event.getDamage() + " " + modifieddmg + " " + crit);
			event.setDamage((event.getDamage() + modifieddmg) * crit);
		}
	}
	
//	@EventHandler(priority=EventPriority.LOW)
//	public void onEntityKill(ExperienceChangeEvent e) {
//		Hero hero = e.getHero();
//		Player player = hero.getPlayer();
//		HeroClass heroClass = e.getHeroClass();
//		
//		if (e.getSource() == HeroClass.ExperienceType.KILLING) {
//			double change = Math.round(e.getExpChange());
//			double current = hero.currentXPToNextLevel(heroClass);
//			
//			double exp = hero.getExperience(heroClass);
//			int level = Properties.getLevel(exp);
//			double maxExperiation = Properties.getTotalExp(level + 1) - Properties.getTotalExp(level);
//			
//			expMessage(player, e.getLocation().subtract(0.0D, -0.5D, 0.0D),
//					change, maxExperiation, Math.round(current) + change);
//		}
//	}
	

}
