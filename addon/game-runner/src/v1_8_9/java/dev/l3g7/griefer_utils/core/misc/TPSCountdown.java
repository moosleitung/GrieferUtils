/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class TPSCountdown {

	private int secondsRemaining;

	public static TPSCountdown replaceFromMins(TPSCountdown old, int minutes) {
		return replaceFromSeconds(old, minutes * 60);
	}

	public static TPSCountdown replaceFromEnd(TPSCountdown old, long end) {
		long ms = end - System.currentTimeMillis();
		return replaceFromSeconds(old, (int) (ms / 1000));
	}

	public static TPSCountdown replaceFromSeconds(TPSCountdown old, int seconds) {
		if (old != null)
			old.destroy();
		return new TPSCountdown(seconds);
	}

	private TPSCountdown(int secondsRemaining) {
		this.secondsRemaining = secondsRemaining;
		EventRegisterer.register(this);
	}

	public int secondsRemaining() {
		return secondsRemaining;
	}

	public void addMinutes(int minutes) {
		secondsRemaining += 60 * minutes;
	}

	public void checkWarning(String title, int warnTime) {
		int remaining = secondsRemaining();
		if (remaining <= 0 || remaining >= warnTime)
			return;

		String time = Util.formatTimeSeconds(remaining, true);
		mc().ingameGUI.displayTitle("§c" + title, null, -1, -1, -1);
		mc().ingameGUI.displayTitle(null, "§c§l" + time, -1, -1, -1);
		mc().ingameGUI.displayTitle(null, null, 0, 2, 3);
	}

	public boolean isExpired() {
		return secondsRemaining <= 0;
	}

	public void destroy() {
		EventRegisterer.unregister(this);
	}

	@EventListener
	private void onTimeUpdate(PacketEvent.PacketReceiveEvent<S03PacketTimeUpdate> event) {
		secondsRemaining--;
	}

}
