/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.chat;


import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderChatEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.NameCache;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ChatLineUtil;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.main.LabyMod;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.misc.Constants.*;

@Singleton
public class MessageSkulls extends Feature {

	private static final ArrayList<Pattern> PATTERNS = new ArrayList<Pattern>(MESSAGE_PATTERNS) {{
		remove(GLOBAL_CHAT_PATTERN);
		add(STATUS_PATTERN);
	}};

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Kopf vor Nachrichten")
		.description("Zeigt den Kopf des Autors vor Nachrichten an.")
		.icon("steve");

	@EventListener
	public void onMsgReceive(MessageEvent.MessageModifyEvent event) {
		for (Pattern pattern : PATTERNS) {
			Matcher matcher = pattern.matcher(event.original.getFormattedText());
			if (!matcher.matches())
				continue;

			String id = "§c   §r";
			event.message = new ChatComponentText(id).appendSibling(event.message);
			return;
		}
	}

	@EventListener
	public void renderSkull(RenderChatEvent event) {
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.disableLighting();
		IChatComponent component = (IChatComponent) event.chatLine.getComponent();
		String formattedText = component.getFormattedText();

		int idStart = formattedText.indexOf("§c   ");
		if (idStart == -1)
			return;

		IChatComponent wholeComponent = ChatLineUtil.getComponentFromLine(event.chatLine);
		if (wholeComponent == null)
			return;

		String msg = wholeComponent.getUnformattedText();

		int startIndex = msg.indexOf('\u2503') + 2;
		int endIndex;
		int arrowIndex = msg.indexOf('\u00bb');

		if (idStart > startIndex)
			return;

		IChatComponent unmodified = ChatLineUtil.getUnmodifiedIChatComponent(wholeComponent);
		if (unmodified == null)
			return;

		String uMsg = unmodified.getUnformattedText();

		if (arrowIndex != -1)
			endIndex = arrowIndex - 1;
		else if (uMsg.startsWith("[Plot-Chat]"))
			endIndex = msg.indexOf(':', startIndex) - 1;
		else if (uMsg.startsWith("[") && uMsg.contains(" -> mir]"))
			endIndex = msg.indexOf('-', startIndex) - 1;
		else if (uMsg.startsWith("[mir -> "))
			endIndex = msg.indexOf(']', startIndex);
		else
			endIndex = msg.indexOf(' ', startIndex);

		String name = msg.substring(startIndex, endIndex);
		NetworkPlayerInfo playerInfo = MinecraftUtil.mc().getNetHandler().getPlayerInfo(NameCache.ensureRealName(name));
		if (playerInfo == null)
			return;

		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();
		drawUtils.bindTexture(playerInfo.getLocationSkin());
		int x = drawUtils.getStringWidth(formattedText.substring(0, idStart)) + (formattedText.startsWith("§r§m§s") ? 2 : 1);
		drawUtils.drawTexture(x, event.y - 8, 32, 32, 32, 32, 8, 8, event.alpha); // First layer
		drawUtils.drawTexture(x, event.y - 8, 160, 32, 32, 32, 8, 8, event.alpha); // Second layer
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
	}

}