/*
 * This file is part of SpoutAPI.
 *
 * Copyright (c) 2011-2012, SpoutDev <http://www.spout.org/>
 * SpoutAPI is licensed under the SpoutDev License Version 1.
 *
 * SpoutAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * SpoutAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spout.api.material;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class MaterialRegistry {
	private final static ConcurrentHashMap<String, Material> nameLookup = new ConcurrentHashMap<String, Material>(1000);
	private final static int MAX_SIZE = 1 << 16;
	@SuppressWarnings("unchecked")
	private final static AtomicReference<Material>[] materialLookup = new AtomicReference[MAX_SIZE];

	static {
		for (int i = 0; i < materialLookup.length; i++) {
			materialLookup[i] = new AtomicReference<Material>();
		}
	}

	/**
	 * Registers the material in the material lookup service
	 * 
	 * @param material to register
	 */
	public static <T extends Material> T register(T material) {		
		if (material.isSubMaterial()) {
			material.getParentMaterial().registerSubMaterial(material);
			nameLookup.put(material.getName().toLowerCase(), material);
			return material;
		} else {
			int id = material.getId();
			if (!materialLookup[id].compareAndSet(null, material)) {
				throw new IllegalArgumentException("Another material is already mapped to id: " + material.getId() + "!");
			} else {
				nameLookup.put(material.getName().toLowerCase(), material);
				return material;
			}
		}
	}

	/**
	 * Gets the material from the given id
	 *
	 * @param id to get
	 * @return material or null if none found
	 */
	public static Material get(short id) {
		if (id < 0 || id >= materialLookup.length) {
			return null;
		}
		return materialLookup[id].get();
	}

	/**
	 * Returns all current materials in the game
	 *
	 * @return an array of all materials
	 */
	public static Material[] values() {
		//TODO: This is wrong, need to count # of registered materials
		HashSet<Material> set = new HashSet<Material>(1000);
		for (int i = 0; i < materialLookup.length; i++) {
			if (materialLookup[i].get() != null) {
				set.add(materialLookup[i].get());
			}
		}
		return set.toArray(new Material[0]);

	}

	/**
	 * Gets the associated material with it's name. Case-insensitive.
	 *
	 * @param name to lookup
	 * @return material, or null if none found
	 */
	public static Material get(String name) {
		return nameLookup.get(name.toLowerCase());
	}
}
