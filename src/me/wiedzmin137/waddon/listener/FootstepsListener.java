package me.wiedzmin137.waddon.listener;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.wiedzmin137.waddon.WAddon;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.collect.ImmutableList;

public class FootstepsListener implements Listener {
	private WAddon plugin;

	public FootstepsListener(WAddon plugin) {
		this.plugin = plugin;
	}
	
	public Set<String> footsteps = new HashSet<String>();
	private List<Material> blocks = Arrays.asList(Material.GRASS, Material.DIRT, Material.SAND, Material.SNOW_BLOCK, Material.CARPET, Material.SOUL_SAND);
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(final PlayerMoveEvent event) {
		
		if (footsteps.contains(event.getPlayer().getName()))
			return;
		
//		if (event.getPlayer().hasPermission("waddon.use"))
//		return;
		
		if (event.getFrom().getX() == event.getTo().getX() && event.getFrom().getZ() == event.getTo().getZ())
			return;
		
		Block below = event.getPlayer().getLocation().subtract(0, 1, 0).getBlock(); //Gets the block they're standing on
		double yOffset = 0.07D;
		
		if (event.getPlayer().getLocation().getBlock().getType() == Material.SNOW || event.getPlayer().getLocation().getBlock().getType() == Material.CARPET || event.getPlayer().getLocation().getBlock().getType() == Material.SOUL_SAND) {
			below = event.getPlayer().getLocation().getBlock();
			yOffset = 0.15D;
			if (event.getPlayer().getLocation().getBlock().getType() == Material.SNOW && event.getPlayer().getLocation().getBlock().getData() == 0 || event.getPlayer().getLocation().getBlock().getType() == Material.CARPET) {
				yOffset = below.getY() - event.getPlayer().getLocation().getY();
				yOffset += 0.15D;
			}
		} else if (event.getPlayer().getLocation().getY() != below.getY() + 1)
			return;
		
		for (Material block : blocks) {
			
			if (!block.equals(below.getType())) {
				continue;
			}
			
			try {
				PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.WORLD_PARTICLES);
				packet.getStrings().write(0, "footstep");
				packet.getFloat().write(0, (float) event.getPlayer().getLocation().getX())
					.write(1, (float) (event.getPlayer().getLocation().getY() + yOffset))
					.write(2, (float) event.getPlayer().getLocation().getZ())
					.write(3, 0F)
					.write(4, 0F)
					.write(5, 0F)
					.write(6, 0F);
				packet.getIntegers().write(0, 1);
				for (Player player : ImmutableList.copyOf(plugin.getServer().getOnlinePlayers())) {
					if (!player.hasPermission("waddon.see"))
						continue;
					if (player.getWorld().equals(event.getPlayer().getPlayer().getWorld())) {
						try {
							ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
				
				footsteps.add(event.getPlayer().getName());
				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
					@Override
					public void run () {
						footsteps.remove(event.getPlayer().getName());
					}
				}, event.getPlayer().isSprinting() ? 7 : 10);
			} catch (Throwable e) {
				WAddon.LOG.warning("Footprints do not work without ProtocolLib!");
				return;
			}
		}
	}
  }
