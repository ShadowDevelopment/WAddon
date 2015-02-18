package me.wiedzmin137.waddon.listener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import me.wiedzmin137.waddon.WAddon;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.xephi.authme.api.API;
import fr.xephi.authme.events.LoginEvent;

public class LayoutListener implements Listener {
	private final WAddon plugin;
	
	public LayoutListener(WAddon plugin) {
		this.plugin = plugin;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) throws EventException, IOException {
		String message = e.getMessage();
		if ((message.contains("./")) && (WAddon.enableDotSlashCleaner == "true"))
		{
			message = message.replaceAll("./", "/");
			e.setMessage(message);
		}
//		if ((WAddon.enableLinkUnderliner == "true") && ((message.contains("http://")) || (message.contains("https://")))) {
//			if ((e.getPlayer().hasPermission("itags.links")) || (e.getPlayer().hasPermission("itags.*")) || (e.getPlayer().isOp())) {
//				String[] words = message.split(" ");
//				for (int x = 0; x < words.length; x++) {
//					if ((words[x].contains(".")) && ((words[x].startsWith("http://")) || (words[x].startsWith("https://")))) {
//						String linkmessage = words[x];
//						message = message.replace(linkmessage, WAddon.parseColor(WAddon.linkColor) + WAddon.parseColor("&n") + linkmessage + " " + WAddon.parseColor("&r") + WAddon.parseColor(WAddon.chatColor));
//					}
//				}
//				e.setMessage(message);
//			}
//		}
		if ((message.contains("#")) && (WAddon.enableHashTags == "true")) {
			if ((e.getPlayer().hasPermission("itags.hashtag")) || (e.getPlayer().hasPermission("itags.*")) || (e.getPlayer().isOp()) || (!e.getPlayer().isOnline())) {
				String[] words = message.split(" ");
				for (int x = 0; x < words.length; x++) {
					String word = words[x].replaceAll("#", "");
					if (words[x].startsWith("#") && word.matches("^[a-zA-Z0-9!]*$") && word.length() < 10) {
						String tagmessage = words[x];
						message = message.replaceAll(tagmessage, WAddon.parseColor(WAddon.hashTagColor) + tagmessage + WAddon.parseColor("&r") + WAddon.parseColor(WAddon.chatColor));
					}
				}
				e.setMessage(message);
			}
		}
		if ((message.contains(WAddon.playerTag)) && (WAddon.enableChatTags == "true")) {
			if ((e.getPlayer().hasPermission("itags.playertag")) || (e.getPlayer().hasPermission("itags.*")) || (e.getPlayer().isOp())) {
				String[] words = message.split(" ");
				for (int x = 0; x < words.length; x++) {
					if ((words[x].startsWith(WAddon.playerTag)) && (words[x].length() > 2)) {
						String tagmessage = words[x];
						tagmessage = tagmessage.replaceAll(WAddon.playerTag, "");
						
						String last = tagmessage.substring(tagmessage.length() - 1);
						String[] apunctuation = { "?", "!", ".", ",", "\"", "'", ")", "}", "]", "/" };
						List<String> punctuation = Arrays.asList(apunctuation);
						String endmark = "";
						if (punctuation.contains(last)) {
							tagmessage = tagmessage.replace(last, "");
						}
						
						if (tagmessage.equalsIgnoreCase("all")) {
							if (e.getPlayer().hasPermission("itags.playertag.all")) {
								for (Player player : Bukkit.getOnlinePlayers()) {
									player.playSound(player.getLocation(), Sound.valueOf(WAddon.chatSound.toUpperCase()), 0.5F, 0.0F);
								}
								if (WAddon.useDisplayNameColors == "true") {
									message = message.replaceAll(WAddon.playerTag + tagmessage + endmark, "All" + WAddon.parseColor("&r") + WAddon.parseColor(WAddon.chatColor) + endmark);
								} else {
									message = message.replaceAll(WAddon.playerTag + tagmessage + endmark, WAddon.parseColor(WAddon.playerTagColor) + WAddon.playerTag + "All" + WAddon.parseColor("&r") + WAddon.parseColor(WAddon.chatColor) + endmark);
								}
							} else {
								return;
							}
						} else {
							Player taggedplayer = Bukkit.getPlayer(tagmessage);
							if (taggedplayer != null) {
								if (WAddon.useDisplayNameColors == "true") {
									message = message.replaceAll(WAddon.playerTag + tagmessage + endmark, taggedplayer.getDisplayName() + WAddon.parseColor("&r") + WAddon.parseColor(WAddon.chatColor) + endmark);
								} else {
									message = message.replaceAll(WAddon.playerTag + tagmessage + endmark, WAddon.parseColor(WAddon.playerTagColor) + WAddon.playerTag + taggedplayer.getName() + WAddon.parseColor("&r") + WAddon.parseColor(WAddon.chatColor) + endmark);
								}
								taggedplayer.playSound(taggedplayer.getLocation(), Sound.valueOf(WAddon.chatSound.toUpperCase()), 0.5F, 0.0F);
							}
						}
					}
				}
				e.setMessage(message);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onSign(SignChangeEvent e) {
		String[] lines = e.getLines();
		int lc = 0;
		for (String l : lines) {
			if ((l.contains(WAddon.playerTag)) && (WAddon.enableSignTags == "true")) {
				if ((e.getPlayer().hasPermission("itags.signtag")) || (e.getPlayer().hasPermission("itags.*")) || (e.getPlayer().isOp())) {
					String[] words = l.split(" ");
					for (int x = 0; x < words.length; x++) {
						if (words[x].startsWith(WAddon.playerTag)) {
							String tagmessage = words[x];
							tagmessage = tagmessage.replaceAll(WAddon.playerTag, "");
							Player taggedplayer = Bukkit.getPlayer(tagmessage);
							if (taggedplayer != null) {
								String taggedname = taggedplayer.getName();
								String playername = e.getPlayer().getName();
								if (WAddon.useDisplayNameColors == "true") {
									taggedname = taggedplayer.getDisplayName();
									playername = e.getPlayer().getDisplayName();
								}
								String newMessage = l.replaceAll(WAddon.playerTag + tagmessage, taggedname);
								
								e.setLine(lc, newMessage);
								if (WAddon.signNotifyType.equals("full")) {
									taggedplayer.sendMessage(ChatColor.GOLD + "[WAddon] " + playername + ChatColor.GOLD + " Has signed you on the sign!");
									taggedplayer.sendMessage(ChatColor.GOLD + "----------------------");
									taggedplayer.sendMessage(ChatColor.GOLD + "-- " + ChatColor.RESET + e.getLine(0));
									taggedplayer.sendMessage(ChatColor.GOLD + "-- " + ChatColor.RESET + e.getLine(1));
									taggedplayer.sendMessage(ChatColor.GOLD + "-- " + ChatColor.RESET + e.getLine(2));
									taggedplayer.sendMessage(ChatColor.GOLD + "-- " + ChatColor.RESET + e.getLine(3));
									taggedplayer.sendMessage(ChatColor.GOLD + "----------------------");
								} else if (WAddon.signNotifyType.equals("mini")) {
									taggedplayer.sendMessage(ChatColor.GOLD + "[WAddon] " + playername + ChatColor.GOLD + " Has signed you on the sign!");
								} else {
									return;
								}
								taggedplayer.playSound(taggedplayer.getLocation(), Sound.valueOf(WAddon.signSound.toUpperCase()), 0.5F, 1.0F);
							}
						}
					}
				}
			}
			lc++;
		}
	}
	
	@EventHandler
	public void playerJoin(LoginEvent event) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(joinMessage(event.getPlayer()));
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void playerJoin(final PlayerJoinEvent event) {
		event.setJoinMessage("");
		new BukkitRunnable() {
			@Override
			public void run() {
				if (API.isAuthenticated(event.getPlayer())) {
					for (Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(joinMessage(event.getPlayer()));
					}
				}
			}
		}.runTaskLaterAsynchronously(plugin, 5);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerQuit(PlayerQuitEvent  event) {
		Player p = event.getPlayer();
		if (API.isAuthenticated(p)) {
			String group = WAddon.permission.getPrimaryGroup(p);
			String chatPrefix = WAddon.chat.getGroupPrefix(p.getWorld(), group);
			String defaultSuffix = "";
			
			if (WAddon.permission.playerInGroup(p, "defaults")) {
				defaultSuffix = "&r&7]";
			}
			
			event.setQuitMessage(StringEscapeUtils.unescapeHtml(t(chatPrefix + p.getDisplayName() + defaultSuffix + " &r&e&oHas left the game! &r&#9785;")));
		} else {
			event.setQuitMessage("");
		}

	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event) {
		Player p = event.getPlayer();
		String group = WAddon.permission.getPrimaryGroup(p);
		String chatPrefix = WAddon.chat.getGroupPrefix(p.getWorld(), group);
		String defaultSuffix = "";
		
		if (WAddon.permission.playerInGroup(p, "defaults")) {
			defaultSuffix = "&r&7]";
		}
		
		event.setLeaveMessage(StringEscapeUtils.unescapeHtml(t(chatPrefix + p.getDisplayName() + defaultSuffix + " &r&e&oHas been kicked! &r&#9785;")));	
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent event) { 
		if (!(event.getEntity() instanceof Player))
			return;
		if (!(event instanceof PlayerDeathEvent))
			return;
		
		PlayerDeathEvent subEvent = (PlayerDeathEvent) event;
		subEvent.setDeathMessage("");
	}

	private String joinMessage(Player player) {
		Player p = player;
		String group = WAddon.permission.getPrimaryGroup(p);
		String chatPrefix = WAddon.chat.getGroupPrefix(p.getWorld(), group);
		String defaultSuffix = "";
		
		if (WAddon.permission.playerInGroup(p, "defaults")) {
			defaultSuffix = "&r&7]";
		}
		
		return StringEscapeUtils.unescapeHtml(t(chatPrefix + p.getDisplayName() + defaultSuffix + " &r&e&oHas joined to game! &r&#9786;"));
	}
	
	private String t(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}
}
