package me.wiedzmin137.waddon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import me.desht.scrollingmenusign.ScrollingMenuSign;
import me.wiedzmin137.waddon.database.DataManager;
import me.wiedzmin137.waddon.database.WPlayerData;
import me.wiedzmin137.waddon.listener.EntityListener;
import me.wiedzmin137.waddon.listener.FootstepsListener;
import me.wiedzmin137.waddon.listener.LayoutListener;
import me.wiedzmin137.waddon.listener.ProtocolListener;
import me.wiedzmin137.waddon.listener.WAddonListener;
import me.wiedzmin137.waddon.util.ConfigCopy;
import me.wiedzmin137.wheroesaddon.util.Utils;
import me.wiedzmin137.wheroesaddon.util.menu.events.ItemClickEvent;
import me.wiedzmin137.wheroesaddon.util.menu.items.BackItem;
import me.wiedzmin137.wheroesaddon.util.menu.items.MenuItem;
import me.wiedzmin137.wheroesaddon.util.menu.menus.ItemMenu;
import me.wiedzmin137.wheroesaddon.util.menu.menus.ItemMenu.Size;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableList;
import com.herocraftonline.heroes.Heroes;

public class WAddon extends JavaPlugin {
	//public static PluginLanguageData lang = new PluginLanguageData(WAddon.getInstance(), ISOCode.pol);
	public static Map<String, String> lang = new HashMap<>();
	public static HashMap<InetAddress, Long> heldAddresses = new HashMap<InetAddress, Long>();
	public static List<UUID> playersJoinedWith = new ArrayList<UUID>();
	
	
	static PluginDescriptionFile pdfFile;
	public static String enableChatTags = "true";
	public static String enableSignTags = "true";
	public static String enableHashTags = "false";
	public static String enableDotSlashCleaner = "true";
	public static String enableLinkUnderliner = "true";
	public static String signNotifyType = "mini";
	public static String playerTag = "#";
	public static String playerTagColor = "&e";
	public static String hashTagColor = "&b";
	public static String linkColor = "&7";
	public static String chatColor = "&f";
	public static String censorHashTags = "true";
	public static String useDisplayNameColors = "false";
	public static String chatSound = "NOTE_PLING";
	public static String signSound = "NOTE_BASS_GUITAR";
	public static String updateNotification = "true";
	public static Integer configVersion = Integer.valueOf(3);
	
	private ItemMenu raceChoose;
	private ItemMenu raceConfirm;
	
	private Map<String, String> raceChooseMap = new HashMap<>();
	private ConfigCopy raceConfig;
	
	public static Permission permission = null;
	public static Chat chat = null;
    public static Economy economy = null;
	
	public static Heroes heroes;
	public static ScrollingMenuSign sms;
	private static WAddon instance;
	
	public int entitySpawnCap;
	public List<EntityType> allowedEntityList;
	public Hashtable<EntityType, Integer> individCapData;
	private EntityListener listener;
	public BarAPI bar;
	
	public final static Logger LOG = Logger.getLogger("Minecraft");
	
	private DataManager dataManager;
	
	@Override
	public void onDisable() {
		CreatureSpawnEvent.getHandlerList().unregister(this);
		PlayerInteractEntityEvent.getHandlerList().unregister(this);
		
		for (Player player : ImmutableList.copyOf(WAddon.getInstance().getServer().getOnlinePlayers())) {
			player.performCommand("lobby");
		}
		
		instance = null;
		getLogger().info("[WAddon] vA0.1 has been disabled!");
	}
	
	@Override
	public void onEnable() {
		instance = this;
		this.saveDefaultConfig();
		initialize();
		PluginManager pm = Bukkit.getServer().getPluginManager();
		heroes = (Heroes) pm.getPlugin("Heroes");
		sms = (ScrollingMenuSign) pm.getPlugin("ScrollingMenuSign");
		
		try {
			raceConfig = new ConfigCopy(this, "raceChoose.yml");
			raceConfig.checkFile(raceConfig.getFile());
			
			initialiseMenu();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if (!new File(getDataFolder(), "config.yml").exists()) {
			saveDefaultConfig();
		}
		
		if (!setupPermissions() || !setupChat() || !setupEconomy()) {
			Bukkit.getLogger().info("Vault dependency not found!");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}
		
		reloadConfiguration();
		if ((!getConfig().isSet("config-version")) || (getConfig().getInt("config-version") < 3))
		{
			File oldfile = new File(getDataFolder(), "config.yml");
			oldfile.delete();
			
			
			saveDefaultConfig();
			
			
			File file = new File(getDataFolder(), "config.yml");
			try
			{
				updateConfig(file);
				reloadConfiguration();
				getLogger().info("Updated config.yml file with new options!");
			}
			catch (Exception e)
			{
				getLogger().warning("Config.yml could not be updated! Please delete it! E01");
				e.printStackTrace();
			}
		}

		setupLanguage();
		
		Commands commandExecutor = new Commands(this);
		getCommand("itags").setExecutor(commandExecutor);
		getCommand("ding").setExecutor(commandExecutor);
		getCommand("townyinfo").setExecutor(commandExecutor);
		getCommand("mayorinfo").setExecutor(commandExecutor);
		getCommand("showsms").setExecutor(commandExecutor);
		getCommand("test").setExecutor(commandExecutor);
		getCommand("itemsgive").setExecutor(commandExecutor);
		getCommand("choose").setExecutor(commandExecutor);
//		getCommand("shop").setExecutor(commandExecutor);
		getCommand("rf").setExecutor(commandExecutor);
		getCommand("raceChoose").setExecutor(commandExecutor);
		getCommand("setRace").setExecutor(commandExecutor);
		getCommand("getStat").setExecutor(commandExecutor);
		
		dataManager = new DataManager(this);
		getServer().getPluginManager().registerEvents(dataManager, this);
		getServer().getPluginManager().registerEvents(new ProtocolListener(this), this);
		getServer().getPluginManager().registerEvents(new LayoutListener(this), this);
		getServer().getPluginManager().registerEvents(new FootstepsListener(this), this);
		getServer().getPluginManager().registerEvents(new WAddonListener(this), this);
		
		bar = new BarAPI(this);
		//PingManager.setPingManager(PingHandler.class);
		
		pdfFile = getDescription();
		
		new UpdateQuickTask().runTaskTimerAsynchronously(this, 5, 5);
		new UpdateSecondTask().runTaskTimer(this, 5, 20);
		new UpdateLongTask().runTaskTimerAsynchronously(this, 5, 200);
		
		getLogger().info(pdfFile.getName() + " v" + pdfFile.getVersion() + " is enabled!");
	}
	
	public void setupLanguage() {
		lang.put("Breeding", "Animals are restless... There is too tightly!");
		lang.put("SpellCritic", "&#9633;&#9633; &b&oMagic critical hit! &r&#9633;&#9633;");
		lang.put("XP", "XP");
		lang.put("Diseases.BrokenLeg", "&4[&6&oDiseases&4] &e&oYou broke your leg!");
		lang.put("Diseases.HealLeg", "&4[&6&oDiseases&4] &e&oYour leg is fine now!");
	}
	
	public static String parseColor(String line) {
		return ChatColor.translateAlternateColorCodes('&', line);
	}
	
	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}
	 
	public void reloadConfiguration() {
		if (!new File(getDataFolder(), "config.yml").exists()) {
			saveDefaultConfig();
		}
		reloadConfig();
		enableChatTags = getConfig().getString("enableChatTags");
		enableSignTags = getConfig().getString("enableSignTags");
		enableHashTags = getConfig().getString("enableHashTags");
		playerTag = getConfig().getString("playerTag");
		playerTagColor = getConfig().getString("playerTagColor");
		hashTagColor = getConfig().getString("hashTagColor");
		linkColor = getConfig().getString("linkColor");
		chatColor = getConfig().getString("chatColor");
		useDisplayNameColors = getConfig().getString("useDisplayNameColors");
		chatSound = getConfig().getString("chatSound");
		signSound = getConfig().getString("signSound");
		signNotifyType = getConfig().getString("signNotifyType");
		censorHashTags = getConfig().getString("censorHashTags");
		enableDotSlashCleaner = getConfig().getString("enableDotSlashCleaner");
		enableLinkUnderliner = getConfig().getString("enableLinkUnderliner");
		updateNotification = getConfig().getString("update-notification");
	}
	
	public void updateConfig(File config) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(config));
		File tempFile = new File(getDataFolder(), "temp.ignore");
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.replaceAll(".*enableChatTags.*", "enableChatTags: " + enableChatTags);
				line = line.replaceAll(".*enableSignTags.*", "enableSignTags: " + enableSignTags);
				line = line.replaceAll(".*enableHashTags.*", "enableHashTags: " + enableHashTags);
				line = line.replaceAll(".*playerTag:.*", "playerTag: '" + playerTag + "'");
				line = line.replaceAll(".*playerTagColor.*", "playerTagColor: '" + playerTagColor + "'");
				line = line.replaceAll(".*hashTagColor.*", "hashTagColor: '" + hashTagColor + "'");
				line = line.replaceAll(".*linkColor.*", "linkColor: '" + linkColor + "'");
				line = line.replaceAll(".*chatColor.*", "chatColor: '" + chatColor + "'");
				line = line.replaceAll(".*useDisplayNameColors.*", "useDisplayNameColors: " + useDisplayNameColors);
				line = line.replaceAll(".*chatSound.*", "chatSound: " + chatSound);
				line = line.replaceAll(".*signSound.*", "signSound: " + signSound);
				line = line.replaceAll(".*signNotifyType.*", "signNotifyType: " + signNotifyType);
				line = line.replaceAll(".*censorHashTags.*", "censorHashTags: " + censorHashTags);
				line = line.replaceAll(".*enableDotSlashCleaner.*", "enableDotSlashCleaner: " + enableDotSlashCleaner);
				line = line.replaceAll(".*enableLinkUnderliner.*", "enableLinkUnderliner: " + enableLinkUnderliner);
				line = line.replaceAll(".*update-notification.*", "update-notification: " + updateNotification);
				line = line.replaceAll(".*config-version.*", "config-version: " + configVersion);
				
				writer.write(line);
				writer.newLine();
			}
		} catch (Exception e) {
			getLogger().warning("Config.yml could not be updated! Please delete it! E02");
			e.printStackTrace();
		} finally {
			reader.close();
			writer.flush();
			writer.close();
			config.delete();
			tempFile.renameTo(config);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void initialize() {
		
		individCapData = new Hashtable<EntityType, Integer>();
		allowedEntityList = new ArrayList<EntityType>();
		entitySpawnCap = this.getConfig().getInt("spawn-cap", 100);
		boolean individualCap = this.getConfig().getBoolean("use-individual-cap", false);
		String breedingFailMessage = this.getConfig().getString("breeding-fail-msg", "The animal cannot breed - it is too crowded!");
		if (listener != null) {
			listener.unregister();
		}
		if (individualCap)
		{
			List<String> entityListLoad = this.getConfig().getStringList("entity-list-individual");
			if (entityListLoad.size() == 0)
			{
				getLogger().info("Entity list is invalid for individual cap loading! Reverting to defaults..");
				
				individCapData.put(EntityType.COW, 100);
				individCapData.put(EntityType.CHICKEN, 100);
				individCapData.put(EntityType.PIG, 100);
				individCapData.put(EntityType.SHEEP, 100);
			}
			else
			{
				for (String entry : entityListLoad)
				{
					String[] temp = entry.split(",");
					int tempint = Integer.parseInt(temp[1].trim());
					EntityType temptype = EntityType.fromName(temp[0].toUpperCase());
					getLogger().info(String.format("%s, %s", temptype, tempint));
					individCapData.put(temptype, tempint);
				}
			}
			
			listener = new EntityListener(this, entitySpawnCap, individCapData, breedingFailMessage);
			getLogger().info("Watching individual entity limits");
		}
		else
		{
			List<String> entityListLoad = this.getConfig().getStringList("entity-list");
			if (entityListLoad.size() == 0)
			{
				getLogger().info("Entity list appears invalid! Reverting to defaults..");
				
				allowedEntityList.add(EntityType.COW);
				allowedEntityList.add(EntityType.CHICKEN);
				allowedEntityList.add(EntityType.PIG);
				allowedEntityList.add(EntityType.SHEEP);
			} else {
				//load list from config
				
				getLogger().info("Loading breeding deny list from config..");
				
				for (String ent : entityListLoad)
				{
						EntityType temp = EntityType.fromName(ent);
						allowedEntityList.add(temp);
				}
			}
			
			listener = new EntityListener(this, entitySpawnCap, allowedEntityList, breedingFailMessage);
			getLogger().info(String.format("Now watching: %s when entity chunk count above %s", allowedEntityList.toString(), entitySpawnCap));
		}	
	}
	
	 private boolean setupPermissions() {
		 RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		 
		 if (permissionProvider != null) {
			 permission = permissionProvider.getProvider();
		 }
	  
		 return (permission != null);
	 }
	 
	 private boolean setupChat() {
		 RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
		 if (chatProvider != null) {
			 chat = chatProvider.getProvider();
		 }
		 
		 return (chat != null);
	 }
	 

	 private boolean setupEconomy() {
		 RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		 if (economyProvider != null) {
			 economy = economyProvider.getProvider();
		 }
		 return (economy != null);
	 }
	 
	@SuppressWarnings("unchecked")
	private void initialiseMenu() {
		YamlConfiguration yml = raceConfig.getYAML();
		
		int latestConfirm = 0;
		Map<Integer, MenuItem> itemsCo = new HashMap<>();
		for (Map<?, ?> map : yml.getMapList("RaceConfirmation")) {
			int number = (int) map.get("Number");
			if (number > latestConfirm) {
				latestConfirm = number;
			}
			if ((boolean) map.get("BackItem")) {
				itemsCo.put(number, new BackItem());
			} else {
				String[] lore = (Utils.u((List<String>) map.get("Lore"))).toArray(new String[0]);
				itemsCo.put(number, new MenuItem(Utils.u((String) map.get("DisplayName")), new ItemStack(Material.getMaterial((String) map.get("Icon"))), lore) {
					@Override
					public void onItemClick(ItemClickEvent event) {
						//TODO add null value checker (shouldn't exist anyway)
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "raceChoose " + event.getPlayer().getName() +  " " + raceChooseMap.get(event.getPlayer().getName()));
						raceChooseMap.remove(event.getPlayer().getName());
						event.setWillClose(true);
					}
				});
			}
		}
		raceConfirm = new ItemMenu(Utils.u(yml.getString("ConfirmName")), Size.fit(latestConfirm), this);
		for (Map.Entry<Integer, MenuItem> entry : itemsCo.entrySet()) {
			raceConfirm.setItem(entry.getKey(), entry.getValue());
		}
		
		int latestChoose = 0;
		Map<Integer, MenuItem> itemsCh = new HashMap<>();
		for (final Map<?, ?> map : yml.getMapList("RaceChoose")) {
			int number = (int) map.get("Number");
			if (number > latestChoose) {
				latestChoose = number;
			}
			String[] lore = (Utils.u((List<String>) map.get("Lore"))).toArray(new String[0]);
			itemsCh.put(number, new MenuItem(Utils.u((String) map.get("DisplayName")), new ItemStack(Material.getMaterial((String) map.get("Icon"))), lore) {
				@Override
				public void onItemClick(ItemClickEvent event) {
					event.setWillClose(true);
					final String playerName = event.getPlayer().getName();
					
					final String className = (String) map.get("RaceName");
					final boolean test = className.equalsIgnoreCase("NONE");
					if (test) return;
					
					Bukkit.getScheduler().scheduleSyncDelayedTask(WAddon.this, new Runnable() {
						@Override
						@SuppressWarnings("deprecation")
						public void run() {
							Player p = Bukkit.getPlayerExact(playerName);
							if (p != null) {
								raceConfirm.open(p);
								raceChooseMap.put(playerName, className);
							}
						}
					}, 3);
				}
			});
		}
		raceChoose = new ItemMenu(Utils.u(yml.getString("ChooseName")), Size.fit(latestChoose), this);
		raceConfirm.setParent(raceChoose);
		for (Map.Entry<Integer, MenuItem> entry : itemsCh.entrySet()) {
			raceChoose.setItem(entry.getKey(), entry.getValue());
		}
		
	}
	 
	public static class UpdateLongTask extends BukkitRunnable {
		@Override
		public void run() {
			for (Player player : ImmutableList.copyOf(WAddon.getInstance().getServer().getOnlinePlayers())) {
				WPlayerData p = WPlayerData.players.get(player.getName());
				int time = p.getBrokenLegTime();
				if (time != 0) {
					if (time - 20 > 0) {
						p.setBrokenLegTime(time - 20);
					} else {
						p.setBrokenLegTime(0);
					}
					if (p.getBrokenLegTime() == 0) {
						p.healBrokenLeg();
						p.getPlayer().sendMessage(Utils.u(lang.get("Diseases.HealLeg")));
					}
				}
			} 
		}
	}
	
	public static class UpdateSecondTask extends BukkitRunnable {
		@Override
		public void run() {
			for (Player player : ImmutableList.copyOf(WAddon.getInstance().getServer().getOnlinePlayers())) {
				WPlayerData p = WPlayerData.players.get(player.getName());
				int stamina = p.getStamina();
				if (stamina < 100) { //TODO make possible to have more than 100 using Durability
					try {
						int regen = me.wiedzmin137.waddon.util.Utils.getRandom(1, 5);
						if (stamina + regen < 100) {
							p.setStamina(stamina + regen);
						} else {
							p.setStamina(100);
						}	
					} catch (NullPointerException e) {
						if (stamina + 2 < 100) {
							p.setStamina(stamina + 2);
						} else {
							p.setStamina(100);
						}	
					}
				}
				DecimalFormat df = new DecimalFormat("0");
				player.setFoodLevel(Integer.valueOf(df.format(p.getStamina())) / 5);
			}
			for (InetAddress a : heldAddresses.keySet()) {
				if (heldAddresses.get(a) <= 1)
					heldAddresses.remove(a);
				else
					heldAddresses.put(a, heldAddresses.get(a)-1);
			}
		}
	}
		
	public static class UpdateQuickTask extends BukkitRunnable {
		@Override
		public void run() {
			for (Player player : ImmutableList.copyOf(WAddon.getInstance().getServer().getOnlinePlayers())) {
				WPlayerData p = WPlayerData.players.get(player.getName());
				if (player.isSprinting()) {
					if (!player.getGameMode().equals(GameMode.CREATIVE) && !(p.getStamina() < 0)) {
						p.setStamina(p.getStamina() - (2 - p.DURABILITY / 15));
						DecimalFormat df = new DecimalFormat("0");
						player.setFoodLevel(Integer.valueOf(df.format(p.getStamina())) / 5);
					}
				}
			}
		}
	}
	
	public DataManager getDataManager() { return dataManager; }
	public ItemMenu getRaceMenu() { return raceChoose; }
	public static WAddon getInstance() { return instance; }
}
