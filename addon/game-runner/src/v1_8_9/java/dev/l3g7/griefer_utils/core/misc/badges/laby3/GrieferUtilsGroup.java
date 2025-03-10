/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby3;

import dev.l3g7.griefer_utils.core.api.misc.server.requests.StaticApiRequest.StaticApiData.SpecialBadge;
import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import net.labymod.main.LabyMod;
import net.labymod.user.group.EnumGroupDisplayType;
import net.labymod.user.group.LabyGroup;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static net.labymod.user.group.EnumGroupDisplayType.ABOVE_HEAD;
import static net.labymod.user.group.EnumGroupDisplayType.BESIDE_NAME;

public class GrieferUtilsGroup extends LabyGroup {

	public static String icon = "icon";

	private final int color_with_labymod;
	private final int color_without_labymod;
	private final String title;

	public GrieferUtilsGroup() {
		this(null, 0xFFFFFF, 0xFFFFFF);
	}

	public GrieferUtilsGroup(SpecialBadge badge) {
		this(badge.title, badge.colorWithoutLabymod, badge.colorWithLabymod);
	}

	public GrieferUtilsGroup(String title, int color_without_labymod, int color_with_labymod) {
		this.color_with_labymod = color_with_labymod;
		this.color_without_labymod = color_without_labymod;
		this.title = title;
	}

	@Override
	public String getDisplayTag() {
		return "§f§lGrieferUtils " + title;
	}

	@Override
	public void renderBadge(double x, double y, double width, double height, boolean small) {
		if (!Badges.showBadges()) {
			super.renderBadge(x, y, width, height, small);
			return;
		}

		Color color = new Color(LabyMod.getSettings().revealFamiliarUsers ? color_with_labymod : color_without_labymod);

		if (icon.equals("icon"))
			GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);

		GlStateManager.enableBlend();
		LabyMod.getInstance().getDrawUtils().bindTexture("griefer_utils/icons/" + icon + ".png");
		LabyMod.getInstance().getDrawUtils().drawTexture(x, y, 255, 255, 8, 8, 1.1f);
		GlStateManager.color(1f, 1f, 1f);
	}

	@Override
	public EnumGroupDisplayType getDisplayType() {
		return title == null ? BESIDE_NAME : ABOVE_HEAD;
	}
}
