/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.bridges;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.mapping.Mapping;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.api.misc.functions.Predicate;
import dev.l3g7.griefer_utils.core.api.misc.functions.Runnable;
import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;

import java.io.File;
import java.util.UUID;
import java.util.function.BiFunction;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@Bridged
public interface LabyBridge {

	LabyBridge labyBridge = FileProvider.getBridge(LabyBridge.class);

	static void run(Runnable laby3, Runnable laby4) {
		(LABY_4.isActive() ? laby4 : laby3).run();
	}

	static <T> T get(Supplier<T> laby3, Supplier<T> laby4) {
		return (LABY_4.isActive() ? laby4 : laby3).get();
	}

	// General information

	boolean obfuscated();

	Mapping activeMapping();

	boolean forge();

	String addonVersion();

	boolean isBeta();

	float partialTicks();

	int chatButtonWidth();

	// Utility methods

	default void notifyMildError(String message) {notify("§e§lFehler ⚠", "§e" + message, 15_000);}

	default void notify(String title, String message) {notify(title, message, 5_000);}

	void notify(String title, String message, int ms);

	/**
	 * Adds a button redirecting to the support discord server, if supported.
	 */
	void notifyError(String message);

	static void display(String message) {labyBridge.displayInChat(message);}

	static void display(String format, Object... args) {display(String.format(format, args));}

	void displayInChat(String message);

	void openWebsite(String url);

	/**
	 * @return whether the file was opened successfully
	 */
	boolean openFile(File file);

	void copyText(String text);

	// Events

	void onJoin(Runnable callback);

	void onQuit(Runnable callback);

	void onMessageSend(Predicate<String> callback);

	void onMessageModify(BiFunction<Object, Object, Object> callback);

	// Specific methods

	/**
	 * Invokes a LabyMod message send event and returns whether it was canceled.
	 */
	boolean trySendMessage(String message);

	/**
	 * Creates a HeaderSetting acting as a padding for dropdowns.
	 * Only required in LabyMod 3, returns null otherwise.
	 */
	HeaderSetting createLaby3DropDownPadding();

	Pair<String, String> getCachedTexture(UUID uuid);

	void openNameHistory(String name);

}
