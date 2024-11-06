/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import net.minecraft.init.Items;

import static net.minecraft.util.IChatComponent.Serializer.componentToJson;

class MessageDumper {

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Nachrichten-Dumper")
		.description("Dumpt eingehende Nachrichten.")
		.icon(Items.name_tag);

	@EventListener(priority = Priority.LOWEST)
	private static void onMessageModify(MessageModifyEvent event) {
		if (enabled.get() && DebugSettings.enabled.get())
			System.out.println(componentToJson(event.original) + " was modified to " + componentToJson(event.message));
	}

}
