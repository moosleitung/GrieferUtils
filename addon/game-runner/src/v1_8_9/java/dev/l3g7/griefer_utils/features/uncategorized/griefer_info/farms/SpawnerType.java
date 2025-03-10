/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.griefer_info.farms;

import net.minecraft.util.StatCollector;

import java.util.HashMap;
import java.util.Map;

public class SpawnerType {

	public static Map<String, SpawnerType> SPAWNER_TYPES = new HashMap<>();
	public static Map<String, SpawnerType> TYPES_BY_ID = new HashMap<>();

	static {
		SPAWNER_TYPES.put("Bruchstein", new SpawnerType("Bruchstein", "tile.stonebrick") {
			public boolean isCobblestone() {
				return true;
			}
		});
		add("Creeper", "Creeper");
		add("Dorfbewohner", "Villager");
		add("Eisengolem", "VillagerGolem");
		add("Enderdrache", "EnderDragon");
		add("Enderman", "Enderman");
		add("Endermilbe", "Endermite");
		add("Fledermaus", "Bat");
		add("Ghast", "Ghast");
		add("Hexe", "Witch");
		add("Höhlenspinne", "CaveSpider");
		add("Huhn", "Chicken");
		add("Kaninchen", "Rabbit");
		add("Kuh", "Cow");
		add("Lohe", "Blaze");
		add("Magmaschleim", "LavaSlime");
		add("Ozelot", "Ozelot");
		add("Pferd", "EntityHorse");
		add("Pilzkuh", "MushroomCow");
		add("Riese", "Giant");
		add("Schaf", "Sheep");
		add("Schleim", "Slime");
		add("Schneegolem", "SnowMan");
		add("Schwein", "Pig");
		add("Silberfischchen", "Silverfish");
		add("Skelett", "Skeleton");
		add("Spinne", "Spider");
		add("Tintenfisch", "Squid");
		add("Wächter", "Guardian");
		add("Wither", "WitherBoss");
		add("Wolf", "Wolf");
		add("Zombie", "Zombie");
		add("Zombie Pigman", "PigZombie");
		add("Kreatur", "Mob");
	}

	private static void add(String name, String translationKey) {
		SPAWNER_TYPES.put(name, new SpawnerType(name, translationKey));
	}

	public final String germanName;
	public final String[] otherNames;
	public final String texture;
	public String id;

	private SpawnerType(String germanName, String texture) {
		this.germanName = germanName;
		this.texture = "mob_icons/minecraft/" + texture.toLowerCase();
		texture = "entity." + texture + ".name";
		otherNames = new String[] {
			StatCollector.translateToLocal(texture).toLowerCase(),
			StatCollector.translateToFallback(texture).toLowerCase()
		};
	}

	public boolean isCobblestone() {
		return false;
	}

	public boolean matchesFilter(String filter) {
		filter = filter.toLowerCase();

		for (String otherName : otherNames)
			if (otherName.contains(filter))
				return true;

		return germanName.toLowerCase().contains(filter);
	}

	public void setId(String id) {
		this.id = id;
		TYPES_BY_ID.put(id, this);
	}


}
