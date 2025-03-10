/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

import java.io.File;

import static com.github.lunatrius.schematica.proxy.ClientProxy.schematic;

public class SchematicaUtil {

	public static final File MATERIAL_FILE = new File(Schematica.proxy.getDirectory("dumps"), "Schematica-materials.txt");

	public static WorldClient getWorld() {
		return schematic;
	}

	public static ISchematic getSchematic() {
		return schematic.getSchematic();
	}

	public static MovingObjectPosition getMovingObjectPosition() {
		return ClientProxy.movingObjectPosition;
	}

	public static BlockPos getPosition() {
		return Reflection.get(schematic, "position");
	}

	public static boolean dontRender() {
		return schematic == null || !schematic.isRendering;
	}

	public static boolean shouldLayerBeRendered(int y) {
		return !schematic.isRenderingLayer || schematic.renderingLayer + getPosition().getY() == y;
	}

	public static void refresh() {
		RenderSchematic.INSTANCE.refresh();
	}

}