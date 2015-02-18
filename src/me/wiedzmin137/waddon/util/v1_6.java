package me.wiedzmin137.waddon.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

/**
* This is the FakeDragon class for BarAPI.
* It is based on the code by SoThatsIt.
*
* http://forums.bukkit.org/threads/tutorial-utilizing-the-boss-health-bar.158018/page-2#post-1760928
*
* @author James Mortemore
*/

public class v1_6 extends FakeDragon {
	private static final Integer EntityID = 6000;

	public v1_6(String name, Location loc) {
		super(name, loc);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object getSpawnPacket() {
		Class<?> mob_class = DragonUtil.getCraftClass("Packet24MobSpawn");
		Object mobPacket = null;
		try {
			mobPacket = mob_class.newInstance();

			Field a = DragonUtil.getField(mob_class, "a");
			a.setAccessible(true);
			a.set(mobPacket, EntityID);// Entity ID
			Field b = DragonUtil.getField(mob_class, "b");
			b.setAccessible(true);
			b.set(mobPacket, EntityType.ENDER_DRAGON.getTypeId());// Mob type
																	// (ID: 64)
			Field c = DragonUtil.getField(mob_class, "c");
			c.setAccessible(true);
			c.set(mobPacket, getX());// X position
			Field d = DragonUtil.getField(mob_class, "d");
			d.setAccessible(true);
			d.set(mobPacket, getY());// Y position
			Field e = DragonUtil.getField(mob_class, "e");
			e.setAccessible(true);
			e.set(mobPacket, getZ());// Z position
			Field f = DragonUtil.getField(mob_class, "f");
			f.setAccessible(true);
			f.set(mobPacket, (byte) ((int) (getPitch() * 256.0F / 360.0F)));// Pitch
			Field g = DragonUtil.getField(mob_class, "g");
			g.setAccessible(true);
			g.set(mobPacket, (byte) (0));// Head
												// Pitch
			Field h = DragonUtil.getField(mob_class, "h");
			h.setAccessible(true);
			h.set(mobPacket, (byte) ((int) (getYaw() * 256.0F / 360.0F)));// Yaw
			Field i = DragonUtil.getField(mob_class, "i");
			i.setAccessible(true);
			i.set(mobPacket, getXvel());// X velocity
			Field j = DragonUtil.getField(mob_class, "j");
			j.setAccessible(true);
			j.set(mobPacket, getYvel());// Y velocity
			Field k = DragonUtil.getField(mob_class, "k");
			k.setAccessible(true);
			k.set(mobPacket, getZvel());// Z velocity

			Object watcher = getWatcher();
			Field t = DragonUtil.getField(mob_class, "t");
			t.setAccessible(true);
			t.set(mobPacket, watcher);
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}

		return mobPacket;
	}

	@Override
	public Object getDestroyPacket() {
		Class<?> packet_class = DragonUtil.getCraftClass("Packet29DestroyEntity");
		Object packet = null;
		try {
			packet = packet_class.newInstance();

			Field a = DragonUtil.getField(packet_class, "a");
			a.setAccessible(true);
			a.set(packet, new int[] { EntityID });
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getMetaPacket(Object watcher) {
		Class<?> packet_class = DragonUtil.getCraftClass("Packet40EntityMetadata");
		Object packet = null;
		try {
			packet = packet_class.newInstance();

			Field a = DragonUtil.getField(packet_class, "a");
			a.setAccessible(true);
			a.set(packet, EntityID);

			Method watcher_c = DragonUtil.getMethod(watcher.getClass(), "c");
			Field b = DragonUtil.getField(packet_class, "b");
			b.setAccessible(true);
			b.set(packet, watcher_c.invoke(watcher));
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getTeleportPacket(Location loc) {
		Class<?> packet_class = DragonUtil.getCraftClass("Packet34EntityTeleport");
		Object packet = null;
		try {
			packet = packet_class.newInstance();

			Field a = DragonUtil.getField(packet_class, "a");
			a.setAccessible(true);
			a.set(packet, EntityID);
			Field b = DragonUtil.getField(packet_class, "b");
			b.setAccessible(true);
			b.set(packet, (int) Math.floor(loc.getX() * 32.0D));
			Field c = DragonUtil.getField(packet_class, "c");
			c.setAccessible(true);
			c.set(packet, (int) Math.floor(loc.getY() * 32.0D));
			Field d = DragonUtil.getField(packet_class, "d");
			d.setAccessible(true);
			d.set(packet, (int) Math.floor(loc.getZ() * 32.0D));
			Field e = DragonUtil.getField(packet_class, "e");
			e.setAccessible(true);
			e.set(packet, (byte) ((int) (loc.getYaw() * 256.0F / 360.0F)));
			Field f = DragonUtil.getField(packet_class, "f");
			f.setAccessible(true);
			f.set(packet, (byte) ((int) (loc.getPitch() * 256.0F / 360.0F)));
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return packet;
	}

	@Override
	public Object getWatcher() {
		Class<?> watcher_class = DragonUtil.getCraftClass("DataWatcher");
		Object watcher = null;
		try {
			watcher = watcher_class.newInstance();

			Method a = DragonUtil.getMethod(watcher_class, "a", new Class<?>[] { int.class, Object.class });
			a.setAccessible(true);

			a.invoke(watcher, 0, isVisible() ? (byte) 0 : (byte) 0x20);
			a.invoke(watcher, 6, health);
			a.invoke(watcher, 7, 0);
			a.invoke(watcher, 8, (byte) 0);
			a.invoke(watcher, 10, name);
			a.invoke(watcher, 11, (byte) 1);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return watcher;
	}

}
