package me.wiedzmin137.waddon.database;

public enum Attribute {
	STRENGTH(10, 5, 15),
	DEXTERITY(10, 10, 10),
	DURABILITY(10, 5, 15),
	INTELLIGENCE(10, 15, 5),
	WISDOM(10, 15, 5);
	
	private int humanVal;
	private int elvVal;
	private int dwarfVal;
	
	private Attribute(int humanVal, int elvVal, int dwarfVal) {
		this.humanVal = humanVal;
		this.elvVal = elvVal;
		this.dwarfVal = dwarfVal;
	}
	
	public int getHumanVal() {
		return humanVal;
	}
	
	public int getElvVal() {
		return elvVal;
	}
	
	public int getDwarfVal() {
		return dwarfVal;
	}
	
	public int getVal(Race race) {
		switch (race) {
			case HUMAN: return getHumanVal();
			case ELV: return getElvVal();
			case DWARF: return getDwarfVal();
			default: return 0;
		}
	}
}
