package me.wiedzmin137.waddon.database;

import java.util.HashMap;

public enum Race {
	HUMAN("Notch", u("§r[§3H§r]§4§o ")),
	ELV("Notch", u("§r[§2E§r]§4§o ")),
	DWARF("Notch", u("§r[§6D§r]§4§o "));
	
	private String skinName;
	private String displayName;
	
	private Race(String skinName, String displayName) {
		this.skinName = skinName;
		this.displayName = displayName;
	}
	
	public String getSkin() {
		return skinName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public static String u(String str) {
		return me.wiedzmin137.wheroesaddon.util.Utils.u(str);
	}
	
	public static HashMap<String, Race> names = new HashMap<>();
}
