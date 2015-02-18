package me.wiedzmin137.waddon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.desht.scrollingmenusign.SMSException;
import me.desht.scrollingmenusign.SMSHandler;
import me.desht.scrollingmenusign.SMSMenu;
import me.desht.scrollingmenusign.enums.SMSMenuAction;
import me.desht.scrollingmenusign.views.SMSInventoryView;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.wiedzmin137.waddon.database.Race;
import me.wiedzmin137.waddon.database.WPlayerData;
import me.wiedzmin137.waddon.util.FancyMessage;
import me.wiedzmin137.waddon.util.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.supavitax.itemlorestats.GenerateFromFile;

public class Commands implements CommandExecutor {
	private final WAddon plugin;
	
	List<FancyMessage> list = new ArrayList<FancyMessage>();
	List<FancyMessage> mayorList = new ArrayList<FancyMessage>();
	GenerateFromFile generator = new GenerateFromFile();
	
	private static List<String> a, b, c, d;
	
	static {
		a = Arrays.asList("&6=============={@name}&6==============",
				"&2Plugin {@name} &2by Saper08 (aka Bednar12)",
				"&7/rf help 1 &e- Informacje ogolne o pluginie",
				"&7/rf help 2 &e- Informacje o systemie kondycji",
				"&7/rf help 3 &e- Informacje o zmianach w PvP",
				"&7/rf messages &e- Wylacza informacje o staminie w chacie",
				"&6==============&2+++++++++++++++&6==============");
		b = Arrays.asList( "&6==============&2Info ogolne&6==============",
				"&eTen plugin zostal stworzony, aby urozmaicic 'tradycyjne' PvP zawarte w Minecrafcie.",
				"&eGlownym celem projektu bylo upodobnienie PvP do prawdziwej walki wrecz, tj. fechtunku.",
				"&eDodatkiem do gry rowniez jest system 'staminy', czyli kondycji.",
				"&eSystem kondycji jest zrobiony na wzor tego z TES V Skyrim, z kilkoma malymi roznicami.",
				"&6==============&2+++++++++++&6==============");
		c = Arrays.asList("&6==============&2Info o kondycji&6==============",
				"&eDo gry wprowadzony jest system tzw. 'staminy' (kondycji),",
				"&eKondycja jest parametrem, takim jak mana, ktory spada w miare wykonywania niektorych czynnosci.",
				"&eTe czynnosci to: ataki, bieg sprintem, oraz blokowanie.",
				"&eJezeli twoja kondycja jest na niskim poziomie, wykonywanie niektorych czynnosci",
				"&emoze byc niemozliwe (sprint) lub nie dawac efektu (blok, atak).",
				"&eKondycja spada stosunkowo szybko, ale rownie szybko sie regeneruje.",
				"&eAlternatywnym sposobem odnowy kondycji jest wypicie wody/mleka",
				"&eMaksymalny poziom kondycji rowna sie 100+5*<poziom gracza>, czyli",
				"&egracz na poziomie trzecim ma 100+5*3=115 pkt. kondycji.",
				"&6==============&2+++++++++++++&6==============");
		d = Arrays.asList("&6==============&2Info o PvP&6==============",
				"&eTen plugin wprowadza kilka znaczacych zmian do PvP.",
				"&eZmiany te dotycza systemu ataku, systemu blokowania, oraz unikow.",
				"&c--------------&2Atak&c--------------",
				"&eZmiany w systemie ataku dotycza systemu staminy (patrz /pvp help 2), oraz",
				"&esystemu zadawanych obrazen. Jesli chodzi o system obrazen, zmiana dotyczy",
				"&ezamiany stalej ilosci obrazen (jak w normalnym MC) na losowy system zadawania",
				"&eobrazen w okreslonych granicach, np. od 5 do 10 serc. Oznacza to, ze mozemy",
				"&ezadac w tym wypadku rownie dobrze 6.5 serca obrazen, jak i 9.5 lub 10.",
				"&c--------------&2Blok&c--------------",
				"&eZmiana w systemie blokowania dotyczy ilosci 'hamowanych' obrazen.",
				"&eW zwyklym Minecrafcie hamowana jest dokladnie polowa obrazen.",
				"&eTu hamowana jest calosc obrazen, ale jest szansa na nieudane",
				"&ezablokowanie ciosu. To daje nam mozliwosc unikniecia obrazen",
				"&edo momentu, w ktorym skonczy sie nam kondycja.",
				"&c--------------&2Unik&c--------------",
				"&eDo gry zostal dodany system unikow, ktory polega na losowej szansie na",
				"&eunik. (W przyszlosci bedzie ona zalezec od poziomu atakowanego)",
				"&eUnik sprawia, ze obrazenia omijaja nas calkowicie, wiec wychodzimy z ciosu",
				"&ebez szwanku.");
	}
	
	private static SMSHandler smsHandler;
	
	Commands(WAddon plugin) {
		this.plugin = plugin;
		smsHandler = WAddon.sms.getHandler();
		
		list.add(new FancyMessage(u("&#9643; &f&eTown info"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oDisplays information about your town")})))
			.command("/t info"));
		list.add(new FancyMessage(u("&#9643; &f&eInformation about the selected town"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oShows all the information about the selected town")})))
			.suggest("/t town"));
		list.add(new FancyMessage(u("&#9643; &f&eInformation of that town"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oShows all the information about the that town")})))
			.command("/towny:town here"));
		list.add(new FancyMessage(u("&#9643; &f&eTowns list"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oShow list of all town")})))
			.command("/towny:town list"));
		list.add(new FancyMessage(u("&#9643; &f&eList of residents"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oShows member online")})))
			.command("/towny:town online"));
		list.add(new FancyMessage(u("&#9643; &f&eDelete from member list"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oDelete you from member list")})))
			.command("/towny:town leave"));
		list.add(new FancyMessage(u("&#9643; &f&eCreate town"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oCreate town")})))
			.suggest("/t new NAME"));
		list.add(new FancyMessage(u("&#9643; &f&eAdd money to bank"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oShow information about your town")})))
			.suggest("/t deposit AMOUNT"));
		list.add(new FancyMessage(u("&#9643; &f&eMayor command"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oShow Mayor command")})))
			.command("/t mayor"));
	
		mayorList.add(new FancyMessage(u("&#9643; &f&eDownload money from town"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; Download money form town to you")})))
			.suggest("/t withdraw AMOUNT"));
		mayorList.add(new FancyMessage(u("&#9643; &f&eLand occupation"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oLand occupation about your area")})))
			.command("/towny:town claim"));
		mayorList.add(new FancyMessage(u("&#9643; &f&eWithdrawal of land"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oWithdraws land about your area")})))
			.command("/towny:town unclaim"));
		mayorList.add(new FancyMessage(u("&#9643; &f&eAdd member/"))
				.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage :&#58;"), u("&#9643; &b&oAdd member about your town")})))
				.suggest("/t add NICK")
			.then(u("&f&eDelete member"))
				.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oDelete member from your town")})))
				.suggest("/t kick NICK"));
		mayorList.add(new FancyMessage(u("&#9643; &f&eTown Settings"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oShow settings of town")})))
			.command("/towny:town set"));
		mayorList.add(new FancyMessage(u("&#9643; &f&eenvironment town"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oShows the available settings about the town environment")})))
			.command("/towny:town toggle"));
		mayorList.add(new FancyMessage(u("&#9643; &f&eAdd rank/"))
				.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oShows member online")})))
				.suggest("/t rank add NICK RANK")
			.then(u("&f&eDelete member rank"))
				.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oShows member online")})))
				.suggest("/t rank remove NICK RANK"));
		mayorList.add(new FancyMessage(u("&#9643; &f&eDelete town"))
			.itemTooltip(getItem(this.generateDescription(new String[] {u("&3&lUsage&#58;"), u("&#9643; &b&oDelete your town")})))
			.command("/towny:town delete"));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("itags")) {
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					if ((sender.hasPermission("itags.reload")) || (sender.hasPermission("itags.*")) || (sender.isOp())) {
						plugin.reloadConfiguration();
						sender.sendMessage(ChatColor.GREEN + "[iTags] Configuration reloaded!");
					} else {
						sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
					}
				} else if (args[0].equalsIgnoreCase("update")) {
					if ((sender.hasPermission("itags.update")) || (sender.isOp())) {
						if (plugin.getConfig().getString("update-notification") == "false") {
							sender.sendMessage(ChatColor.RED + "This command is disabled in the config!");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "You do not have permissions to perform this command");
					}
				} else {
					sender.sendMessage(ChatColor.YELLOW + "-- " + WAddon.pdfFile.getName() + " v" + WAddon.pdfFile.getVersion() + " --");
					sender.sendMessage(ChatColor.RED + "/itags reload - Reload Config");
					sender.sendMessage(ChatColor.RED + "/itags update - Updates to latest version");
					sender.sendMessage(ChatColor.RED + "/ding [name] [pitch] - Get attention of players");
				}
			} else {
				sender.sendMessage(ChatColor.YELLOW + "-- " + WAddon.pdfFile.getName() + " v" + WAddon.pdfFile.getVersion() + " --");
				sender.sendMessage(ChatColor.RED + "/itags reload - Reload Config");
				sender.sendMessage(ChatColor.RED + "/itags update - Updates to latest version");
				sender.sendMessage(ChatColor.RED + "/ding [name] [pitch] - Get attention of players");
			}
			return true;
		} 
		if (cmd.getName().equalsIgnoreCase("ding")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command cannot be used from console.");
				return false;
			} if ((sender.hasPermission("itags.ding")) || (sender.hasPermission("itags.*")) || ((sender.isOp()) && ((sender instanceof Player)))) {
				Player fromPlayer = (Player)sender;
				Player toPlayer = (Player)sender;
				int inote = 0;
				if ((args.length == 2) && (!WAddon.isNumeric(args[0])) && (WAddon.isNumeric(args[1]))) {
					if (Bukkit.getPlayer(args[0]) == null) {
						fromPlayer.sendMessage(ChatColor.RED + "Player not found!");
					} else {
						toPlayer = Bukkit.getPlayer(args[0]);
					}
					inote = Integer.valueOf(args[1]).intValue() + 14;
					
					float dnote = Float.valueOf(inote).floatValue();
					float note = dnote / 28.0F;
					
					toPlayer.getWorld().playSound(toPlayer.getLocation(), Sound.NOTE_PLING, 0.6F, note);
					toPlayer.sendMessage(ChatColor.GOLD + "Hey you! " + fromPlayer.getDisplayName() + ChatColor.GOLD + " wants your attention!");
					fromPlayer.sendMessage(ChatColor.GOLD + "A ding has been sent to " + toPlayer.getDisplayName() + ChatColor.GOLD + "!");
				} else if (args.length == 1) {
					if (WAddon.isNumeric(args[0])) {
						inote = Integer.valueOf(args[0]).intValue() + 14;
						
						float dnote = Float.valueOf(inote).floatValue();
						float note = dnote / 28.0F;
						
						toPlayer.playSound(toPlayer.getLocation(), Sound.NOTE_PLING, 0.6F, note);
						toPlayer.sendMessage(ChatColor.GOLD + "Ding!");
					} else {
						if (Bukkit.getPlayer(args[0]) == null) {
							fromPlayer.sendMessage(ChatColor.RED + "Player not found!");
						} else {
							toPlayer = Bukkit.getPlayer(args[0]);
						}
						float dnote = Float.valueOf(inote).floatValue();
						float note = dnote / 28.0F;
						
						toPlayer.playSound(toPlayer.getLocation(), Sound.NOTE_PLING, 0.6F, note);
						toPlayer.sendMessage(ChatColor.GOLD + "Hey you! " + fromPlayer.getDisplayName() + ChatColor.GOLD + " wants your attention!");
						fromPlayer.sendMessage(ChatColor.GOLD + "A ding has been sent to " + toPlayer.getDisplayName() + ChatColor.GOLD + "!");
					}
				} else if (args.length == 0) {
					float dnote = Float.valueOf(inote).floatValue();
					float note = dnote / 28.0F;
					
					toPlayer.playSound(toPlayer.getLocation(), Sound.NOTE_PLING, 0.6F, note);
					fromPlayer.sendMessage(ChatColor.GOLD + "Ding!");
				} else {
					fromPlayer.sendMessage(ChatColor.RED + "Incorrect Syntax: /ding [name] [pitch]");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
			}
			return true;
		} 
		if (cmd.getName().equalsIgnoreCase("mayorinfo")) {
			sender.sendMessage(ChatColor.GOLD + "=" + ChatColor.RESET + ChatColor.BOLD + "._______o_______==" + ChatColor.GOLD + "[" + ChatColor.DARK_RED + "Mayor" 
					+ ChatColor.GOLD + "]" + ChatColor.RESET + ChatColor.BOLD + "==_______o_______." + ChatColor.GOLD + "=");
				
			FancyMessage infoUzycie = new FancyMessage(u("&b&oHold the mouse to get information, "))
				.tooltip(u("Exactly"))
				.then(u("&b&oClick to use"))
				.suggest(u("Exactly"));
				
			for (FancyMessage msg : mayorList) {
				if (mayorList.isEmpty()) {
					sender.sendMessage("Error!");
				} else {
					msg.send(sender);		
				}
			}
			infoUzycie.send(sender);
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("townyinfo")) {
			sender.sendMessage(ChatColor.GOLD + "=" + ChatColor.RESET + ChatColor.BOLD + "._______o_______==" + ChatColor.GOLD + "[" + ChatColor.DARK_RED + "Towns" 
					+ ChatColor.GOLD + "]" + ChatColor.RESET + ChatColor.BOLD + "==_______o_______." + ChatColor.GOLD + "=");
				
			FancyMessage infoUzycie = new FancyMessage(u("&b&oHold the mouse to get information, "))
				.tooltip(u("Good"))
				.then(u("&b&oClick to use"))
				.suggest(u("Good"));
				
			for (FancyMessage msg : list) {
				msg.send(sender);
			}
			infoUzycie.send(sender);
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("showsms")) {
			if (args.length < 1) 
				return false;
			SMSMenu menu = null;
			try {
				menu = smsHandler.getMenu(args[0]);
			} catch (SMSException e) {
				sender.sendMessage("[WAddon] There is no menu with name " + args[0]);
				return false;
			}

			SMSInventoryView view = null;
			try {
				view = (SMSInventoryView) smsHandler.getViewManager().getView(args[0]);
			} catch (SMSException e) {
				view = new SMSInventoryView(args[0], menu);
				view.update(menu, SMSMenuAction.REPAINT);
				smsHandler.getViewManager().registerView(view);
			}
			view.setAutosave(true);

			view.toggleGUI(WAddon.getInstance().getServer().getPlayer(args[1]));
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("rf")) {
			if (args[0].equalsIgnoreCase("help")) {
				switch (args[1]) {
				case "1": for (String string : b) sender.sendMessage(string); break;
				case "2": for (String string : c) sender.sendMessage(string); break;
				case "3": for (String string : d) sender.sendMessage(string); break;
				default: for (String string : a) sender.sendMessage(string);
				}
				return true;
			} else {
				for (String string : a) {
					sender.sendMessage(string);
					return true;
				}
			}
		}
		if (cmd.getName().equalsIgnoreCase("itemsgive")) {
			if (sender.hasPermission("start")) { 
				ConsoleCommandSender cSender = Bukkit.getConsoleSender();
				String name = args[1];
				Player p = Bukkit.getPlayer(name);
				p.sendMessage("[Test]");
				PlayerInventory i = p.getInventory();
				
				ItemStack helmet;
				ItemStack chestPlate;
				ItemStack leggings;
				ItemStack boots;
				
				if (args[0].equalsIgnoreCase("Ranger")) {
					Bukkit.dispatchCommand(cSender, "ils give " + name + " Apprentice_bow");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_chain_chest");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_chainma_helmet");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_leather_leggings");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_leather_boots" );
					Bukkit.dispatchCommand(cSender, "essentials:give " + name + " arrow 256" );
					helmet = generator.importWeapon("InitialRangerHelmet", "noChange", name);
					chestPlate = generator.importWeapon("InitialRangerChestplate", "noChange", name);
					leggings = generator.importWeapon("InitialRangerLeggings", "noChange", name);
					boots = generator.importWeapon("InitialRangerBoots", "noChange", name);
					i.setHelmet(helmet);
					i.setChestplate(chestPlate);
					i.setLeggings(leggings);
					i.setBoots(boots);
					Utils.takeOne(p, helmet);
					Utils.takeOne(p, chestPlate);
					Utils.takeOne(p, leggings);
					Utils.takeOne(p, boots);
					return true;
				} else if (args[0].equalsIgnoreCase("Rogue")) {
					Bukkit.dispatchCommand(cSender, "ils give " + name + " Apprentice_sword");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_gold_helmet");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_gold_chest");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_leather_leggings");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_leather_boots");
					Bukkit.dispatchCommand(cSender, "essentials:give " + name + " potion:8270 3");
					Bukkit.dispatchCommand(cSender, "essentials:give " + name + " potion:8258 3");
					helmet = generator.importWeapon("InitialRogueHelmet", "noChange", name);
					chestPlate = generator.importWeapon("InitialRogueChestplate", "noChange", name);
					leggings = generator.importWeapon("InitialRogueLeggings", "noChange", name);
					boots = generator.importWeapon("InitialRogueBoots", "noChange", name);
					i.setHelmet(helmet);
					i.setChestplate(chestPlate);
					i.setLeggings(leggings);
					i.setBoots(boots);
					Utils.takeOne(p, helmet);
					Utils.takeOne(p, chestPlate);
					Utils.takeOne(p, leggings);
					Utils.takeOne(p, boots);
					return true;
				} else if (args[0].equalsIgnoreCase("Mage")) {
					Bukkit.dispatchCommand(cSender, "ils give " + name + " Stone_sword");
					Bukkit.dispatchCommand(cSender, "ils give " + name + " Apprentice_magic_wand");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_leather_helmet");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_leather_chest");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_leather_leggings");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_leather_boots");
					Bukkit.dispatchCommand(cSender, "essentials:give " + name + " golden_apple 2");
					helmet = generator.importWeapon("InitialMageHelmet", "noChange", name);
					chestPlate = generator.importWeapon("InitialMageChestplate", "noChange", name);
					leggings = generator.importWeapon("InitialMageLeggings", "noChange", name);
					boots = generator.importWeapon("InitialMageBoots", "noChange", name);
					i.setHelmet(helmet);
					i.setChestplate(chestPlate);
					i.setLeggings(leggings);
					i.setBoots(boots);
					Utils.takeOne(p, helmet);
					Utils.takeOne(p, chestPlate);
					Utils.takeOne(p, leggings);
					Utils.takeOne(p, boots);
					return true;
				} else if (args[0].equalsIgnoreCase("Knight")) {
					Bukkit.dispatchCommand(cSender, "ils give " + name + " Apprentice_Axe");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_chainmai_helmet");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_iron_chest");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_chainmail_leggings");
//					Bukkit.dispatchCommand(cSender, "ils give " + name + " Training_chainmail_boots");
					helmet = generator.importWeapon("InitialKnightHelmet", "noChange", name);
					chestPlate = generator.importWeapon("InitialKnightChestplate", "noChange", name);
					leggings = generator.importWeapon("InitialKnightLeggings", "noChange", name);
					boots = generator.importWeapon("InitialKnightBoots", "noChange", name);
					i.setHelmet(helmet);
					i.setChestplate(chestPlate);
					i.setLeggings(leggings);
					i.setBoots(boots);
					Utils.takeOne(p, helmet);
					Utils.takeOne(p, chestPlate);
					Utils.takeOne(p, leggings);
					Utils.takeOne(p, boots);
					return true;
				} else {
					return false;
				}
			}
		}
		if (cmd.getName().equalsIgnoreCase("choose")) {
			Bukkit.dispatchCommand(sender, "showsms ClassesOptional " + sender.getName());
			return true;
		}
//		if (cmd.getName().equalsIgnoreCase("shop")) {
//			Bukkit.dispatchCommand(sender, "showsms Shop " + sender.getName());
//			return true;
//		}
		if (cmd.getName().equalsIgnoreCase("raceChoose")) {
			if (sender.hasPermission("start")) {
				WPlayerData wp = WPlayerData.players.get(args[0]);
				wp.setRace(Race.valueOf(args[1]));
				Race.names.put(args[0], Race.valueOf(args[1]));
				plugin.getDataManager().savePlayer(wp);
				return true;
			}
		}
		if (cmd.getName().equalsIgnoreCase("setRace")) {
			if (sender.hasPermission("start")) {
				if (Race.names.get(args[0]) == null) {
					plugin.getRaceMenu().open((Player) sender);
					return true;
				} else {
					if (!(sender instanceof ConsoleCommandSender)) {
						plugin.getRaceMenu().open((Player) sender);
					}
					return true;
				}
			}
		}
		if (cmd.getName().equalsIgnoreCase("getStat")) {
			WPlayerData wp = WPlayerData.players.get(sender.getName());
			if (wp != null) {
				sender.sendMessage(Race.names.toString());
				sender.sendMessage(wp
						.getRace()
						.toString());
				sender.sendMessage(wp.STRENGTH + ", " + wp.DEXTERITY + ", " + wp.DURABILITY + ", " + wp.INTELLIGENCE + ", " + wp.WISDOM);
				return true;
			} else {
				sender.sendMessage("WPlayerData returns null! [Err: 1]");
				return true;
			}
		}
		if (cmd.getName().equalsIgnoreCase("test")) {
			if (args.length > 1) {
				//plugin.factory.changeDisplay(args[0], args[1], args[2]);
				Disguise d = new PlayerDisguise(args[0]);
				final PlayerWatcher pw = (PlayerWatcher) d.getWatcher();
				pw.setSkin(args[1]);
				return true;
			}
		}
		return false;
	}
	
	private static String u(String str) {
		return Utils.u(str);
	}
	
	private String[] generateDescription(String[] fullDescription) {
		String[] str = new String[fullDescription.length];
		for (int i = 0; i < fullDescription.length; i++) {
			str[i] = fullDescription[i];
		}
		return str;
	}
	
	private static ItemStack getItem(String... content) {
		ItemStack i = new ItemStack(Material.SNOW, 1, (short) 0);
		ItemMeta meta = i.getItemMeta();
		if (meta != null) {
			if (content.length > 0) {
				meta.setDisplayName(content[0]);
			}
			if (content.length > 1) {
				ArrayList<String> list = new ArrayList<String>();
				for (int index = 1; index < content.length; index++) {
					list.add(ChatColor.WHITE + content[index]);
				}
				meta.setLore(list);
			}
			i.setItemMeta(meta);
		}
		return i;
	}
	
	public static int remove(Inventory inventory, Material mat, int amount) {
		ItemStack[] contents = inventory.getContents();
		int removed = 0;
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item == null || !item.getType().equals(mat)) {
				continue;
			}
			int remove = item.getAmount() - amount - removed;
 
			if (removed > 0) {
				removed = 0;
			}
 
			if (remove <= 0) {
				removed += Math.abs(remove);
				contents[i] = null;
			} else {
				item.setAmount(remove);
			}
		}
		return removed;
	}
	
	public static int contains(Inventory inventory, Material mat, int amount) {
		ItemStack[] contents = inventory.getContents();
		int searchAmount = 0;
		for (ItemStack item : contents) {
			if (item == null || !item.getType().equals(mat)) {
				continue;
			}
			searchAmount += item.getAmount();
		}
		return searchAmount - amount;
    }
}
