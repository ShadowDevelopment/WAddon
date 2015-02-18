package me.wiedzmin137.waddon.listener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.wiedzmin137.waddon.WAddon;
import me.wiedzmin137.waddon.util.Utils;
import net.minecraft.server.v1_7_R4.ContainerMerchant;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.IMerchant;
import net.minecraft.server.v1_7_R4.MerchantRecipe;
import net.minecraft.server.v1_7_R4.MerchantRecipeList;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.PacketDataSerializer;
import net.minecraft.util.io.netty.buffer.Unpooled;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

import fr.xephi.authme.api.API;

public class ProtocolListener implements Listener {
	private WAddon p;
	private Field cC;
	
	private List<WrappedGameProfile> message = new ArrayList<WrappedGameProfile>();
	
	String motd = Utils.u("&6[&4RPG&6] &f&lShadowKingdom &b- Work in progress...\n&f&l&#32;&#32;&#32;&#32;&#32; &f&oHold the mouse on the number of players!");
	List<String> string = Arrays.asList(
			Utils.u("&6[&4------------------------&6]"),
			Utils.u("       &4&lShadowKingdom"),
			Utils.u("&#9725; &c&oCreate Empire!"),
			Utils.u("&#9725; &c&oLearn skills!"),
			Utils.u("&6[&4------------------------&6]"),
			Utils.u("        &4&lInformation"),
			Utils.u("&#9725; &c&oNon-Premium &nRPG&c&o server,"),
			Utils.u("&#9725; &c&oClasses, Magic," /*Attributes"*/),
			Utils.u("&#9725; &c&oStamina, Reworked fight,"),
			Utils.u("&#9725; &c&oNew items,"),
			Utils.u("&#9725; &c&oTwo worlds - PvP, PvE,"),
			Utils.u("&#9725; &c&oToDo - Attributes,"),
			Utils.u("&#9725; &c&oToDo - Skilltress,"),
			Utils.u("&#9725; &c&oToDo - Pets,"),
			Utils.u("&#9725; &c&oToDo - Races,"),
			Utils.u("&6[&4------------------------&6]"),
			Utils.u("&c&oServer is in &nBETA&c&o,"),
			Utils.u("&#9725; &c&oReport any bugs"),
			Utils.u("&6[&4------------------------&6]"));
	
	public ProtocolListener(WAddon plugin) {
		this.p = plugin;
		
		registerProtocols();
	}
	
	public void registerProtocols() {
		try {
			cC = EntityPlayer.class.getDeclaredField("containerCounter");
			cC.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e1) {}
		
		Set<PacketType> packets = new HashSet<PacketType>();
		packets.add(PacketType.Play.Server.SET_SLOT);
		packets.add(PacketType.Play.Server.WINDOW_ITEMS);
		packets.add(PacketType.Play.Server.CUSTOM_PAYLOAD);

		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(p, packets) {
			@SuppressWarnings("unchecked")
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				PacketType type = packet.getType();
				if(type == PacketType.Play.Server.WINDOW_ITEMS) {
					try {
						ItemStack[] read = packet.getItemArrayModifier().read(0);
						for(int i = 0; i < read.length; i++) {
							read[i] = removeAttributes(read[i]);
						}
						packet.getItemArrayModifier().write(0, read);
					}
					catch(FieldAccessException e) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
					}
				}
				else if(type == PacketType.Play.Server.CUSTOM_PAYLOAD) {
					if(!packet.getStrings().read(0).equalsIgnoreCase("MC|TrList")) {
						return;
					}
					try {
						EntityPlayer p = ((CraftPlayer)event.getPlayer()).getHandle();
						ContainerMerchant cM = ((ContainerMerchant)p.activeContainer);
						Field fieldMerchant = cM.getClass().getDeclaredField("merchant");
						fieldMerchant.setAccessible(true);
						IMerchant imerchant = (IMerchant)fieldMerchant.get(cM);

						MerchantRecipeList merchantrecipelist = imerchant.getOffers(p);
						MerchantRecipeList nlist = new MerchantRecipeList();
						for(Object orecipe : merchantrecipelist) {
							MerchantRecipe recipe = (MerchantRecipe)orecipe;
							int uses = recipe.i().getInt("uses");
							int maxUses = recipe.i().getInt("maxUses");
							MerchantRecipe nrecipe = new MerchantRecipe(removeAttributes(recipe.getBuyItem1()), 
									removeAttributes(recipe.getBuyItem2()), removeAttributes(recipe.getBuyItem3()));
							nrecipe.a(maxUses - 7);
							for(int i = 0; i < uses; i++) {
								nrecipe.f();
							}
							nlist.add(nrecipe);
						}
						
						PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());
						packetdataserializer.writeInt(cC.getInt(p));
						nlist.a(packetdataserializer);
						byte[] b = packetdataserializer.array();
						packet.getByteArrays().write(0, b);
						packet.getIntegers().write(0, b.length);
					}
					catch(NoSuchFieldException e) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
					}
					catch(SecurityException e) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
					}
					catch(IllegalArgumentException e) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
					}
					catch(IllegalAccessException e) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
					}
					catch(FieldAccessException e) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
					}
				}
				else {
					try {
						packet.getItemModifier().write(0, removeAttributes(packet.getItemModifier().read(0)));
					}
					catch(FieldAccessException e) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
					}
				}
			}
			
		});
		
//		ProtocolLibrary.getProtocolManager().addPacketListener( new PacketAdapter(plugin, ListenerPriority.NORMAL, new PacketType[] { PacketType.Play.Server.ENTITY_METADATA }) {
//			@Override
//			public void onPacketSending(PacketEvent event){
//				try {
//					Player observer = event.getPlayer();
//					StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());
//					Entity entity = entityModifer.read(0);
//					if ((entity != null) && (observer != entity) && ((entity instanceof LivingEntity)) && 
//						((!(entity instanceof EnderDragon)) || (!(entity instanceof Wither))) && (
//						(entity.getPassenger() == null) || (entity.getPassenger() != observer))) {
//							event.setPacket(event.getPacket().deepClone());
//							StructureModifier<List<WrappedWatchableObject>> watcher = event.getPacket().getWatchableCollectionModifier();
//							for (WrappedWatchableObject watch : watcher.read(0)) {
//								if ((watch.getIndex() == 6) && (((Float)watch.getValue()).floatValue() > 0.0F)) {
//									watch.setValue(Float.valueOf(new Random().nextInt((int)((LivingEntity)entity).getMaxHealth()) + new Random().nextFloat()));
//							}
//						}
//					}
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
		
		for (String str : string) {
			message.add(new WrappedGameProfile("1", str));
		}
		ProtocolLibrary.getProtocolManager().addPacketListener( new PacketAdapter(p, ListenerPriority.NORMAL, new PacketType[] { PacketType.Status.Server.OUT_SERVER_INFO }) {
			@Override
			public void onPacketSending(PacketEvent event) {
				event.getPacket().getServerPings().read(0).setPlayers(message);
			}
		});
	}
		
	private ItemStack removeAttributes(ItemStack i) {
		if(i == null) {
			return i;
		}
		if(i.getType() == Material.BOOK_AND_QUILL) {
			return i;
        }
		ItemStack item = i.clone();
		net.minecraft.server.v1_7_R4.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag;
		if(!nmsStack.hasTag()) {
			tag = new NBTTagCompound();
			nmsStack.setTag(tag);
		}
		else {
			tag = nmsStack.getTag();
		}
		NBTTagList am = new NBTTagList();
		tag.set("AttributeModifiers", am);
		nmsStack.setTag(tag);
		return CraftItemStack.asCraftMirror(nmsStack);
	}
	
	private net.minecraft.server.v1_7_R4.ItemStack removeAttributes(net.minecraft.server.v1_7_R4.ItemStack i) {
		if(i == null) {
			return i;
		}
		if(net.minecraft.server.v1_7_R4.Item.getId(i.getItem()) == 386) {
			return i;
		}
		net.minecraft.server.v1_7_R4.ItemStack item = i.cloneItemStack();
		NBTTagCompound tag;
		if(!item.hasTag()) {
			tag = new NBTTagCompound();
			item.setTag(tag);
		}
		else {
			tag = item.getTag();
		}
		NBTTagList am = new NBTTagList();
		tag.set("AttributeModifiers", am);
		item.setTag(tag);
		return item;
	}
	
	@EventHandler
	public void onMount(final VehicleEnterEvent event) {
		if ((event.getEntered() instanceof Player)) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(p, new Runnable() {
				@Override
				public void run() {
					if ((event.getVehicle().isValid()) && (event.getEntered().isValid())) {
						ProtocolLibrary.getProtocolManager().updateEntity(event.getVehicle(), 
								Arrays.asList(new Player[] { (Player)event.getEntered() }));
					}
				}
			});
		}
	}
	
	private boolean worldIsSurvival(String worldname) {
		return (worldname.equalsIgnoreCase("Spawn") || worldname.equalsIgnoreCase("RPG"));
	}
	
	private boolean worldIsCreative(String worldname) {
		return (worldname.equalsIgnoreCase("worldname") || worldname.equalsIgnoreCase("RPG_Quests"));
	}
	
	private boolean worldIsAdventure(String worldname) {
		return (worldname.equalsIgnoreCase("Dungeons"));
	}
	
	@EventHandler
	public void onPlayerJoin(final fr.xephi.authme.events.LoginEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = event.getPlayer();
				String world = player.getWorld().getName().toString();
				if (worldIsSurvival(world)) {
					player.setGameMode(GameMode.SURVIVAL);
				} else if (worldIsAdventure(world)) {
					player.setGameMode(GameMode.ADVENTURE);
				} else if (worldIsCreative(world)) {
					player.setGameMode(GameMode.CREATIVE);
				}
			}
		}.runTaskLater(WAddon.getInstance(), 5);
	}
	
	@EventHandler
	public void ChangWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		if (API.isAuthenticated(player)) {
			String world = player.getWorld().getName().toString();
			if (worldIsSurvival(world)) {
				player.setGameMode(GameMode.SURVIVAL);
			} else if (worldIsAdventure(world)) {
				player.setGameMode(GameMode.ADVENTURE);
			} else if (worldIsCreative(world)) {
				player.setGameMode(GameMode.CREATIVE);
			}
		}
	}
	
	@EventHandler
	public void onServerPing(ServerListPingEvent e) {
		e.setMotd(motd);
	}
}

