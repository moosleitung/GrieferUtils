/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.uncategorized.debug.thread.ThreadDumper;
import net.minecraft.init.Blocks;

public class DebugSettings {

	private static final SwitchSetting disableEventBus = SwitchSetting.create()
		.name("Eventbus deaktivieren")
		.description("Deaktiviert GrieferUtils' Eventbus.")
		.icon(Blocks.tnt);

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Debug")
		.description("Verschiedene Werkzeuge, um GrieferUtils zu debuggen.", "Es wird empfohlen, dies nur auf Anweisung vom GrieferUtils-Team zu aktivieren.")
		.icon(Blocks.command_block)
		.subSettings(
			PacketDumper.enabled,
			ThreadDumper.enabled,
			RecraftLogger.enabled,
			EventBusProfiler.enabled,
			ItemDumper.key,
			HeaderSetting.create(),
			disableEventBus)
		.callback(b -> {
			if (b) {
				ThreadDumper.tryStartThread();
				EventBusProfiler.updateProfiler();
			}
		});

	static {
		disableEventBus.callback(b -> Event.fireEvents = !enabled.get() || !b);
	}
}
