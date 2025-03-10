/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.VersionComparator;
import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher;
import dev.l3g7.griefer_utils.core.auto_update.AutoUpdater;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.core.misc.gui.guis.ChangelogScreen;
import net.labymod.api.Textures;
import net.labymod.main.ModTextures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.auto_update.ReleaseInfo.ReleaseChannel.BETA;

@Singleton
public class Changelog {

	public static final CategorySetting changelog = CategorySetting.create()
		.name("§eChangelog")
		.description("§eVerbindet...")
		.icon("white_scroll")
		.disable()
		.subSettings();

	@EventListener
	private void onStaticData(StaticDataReceiveEvent event) {
		List<BaseSetting<?>> entries = new ArrayList<>();

		if (Settings.releaseChannel.get() == BETA)
			ChangelogScreen.setData(LabyBridge.labyBridge.addonVersion(), event.data.changelog.beta.substring("Changelog:".length()));

		for (Map.Entry<String, String> entry : event.data.changelog.all.entrySet()) {
			if (!ChangelogScreen.hasData()) {
				ChangelogScreen.setData(
					entry.getKey(),
					entry.getValue().substring("Changelog:".length())
				);
			}

			String title = "§l" + entry.getKey();

			Function<ButtonSetting, ButtonSetting> addIconFunc = LABY_4.isActive() ? this::addIconLaby4 : this::addIconLaby3;
			entries.add(addIconFunc.apply(ButtonSetting.create()
				.name(" " + title)
				.callback(() -> {
					ChangelogScreen.setData(
						entry.getKey(),
						entry.getValue().substring("Changelog:".length())
					);
					ChangelogScreen.trigger(true);
				})));
		}

		entries.sort(Comparator.comparing(BaseSetting::name, new VersionComparator()));
		changelog.subSettings(entries);

		changelog.name("Changelog")
			.description("Was sich in den einzelnen Updates von GrieferUtils verändert hat.")
			.enable();
	}

	private ButtonSetting addIconLaby4(ButtonSetting button) { // TODO refactor
		return button.buttonIcon(Textures.SpriteCommon.SETTINGS);
	}

	private ButtonSetting addIconLaby3(ButtonSetting button) {
		return button.buttonIcon(ModTextures.BUTTON_ADVANCED);
	}

	@OnEnable
	public void onEnable() {
		if (AutoUpdater.hasUpdated && ConfigPatcher.versionChanged)
			ChangelogScreen.trigger(false);
	}
}