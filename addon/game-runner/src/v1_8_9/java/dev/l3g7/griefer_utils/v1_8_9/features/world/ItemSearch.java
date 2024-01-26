/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.features.world;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.render.DrawGuiContainerForegroundLayerEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.ModTextField;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;
import static org.lwjgl.input.Keyboard.KEY_A;
import static org.lwjgl.input.Keyboard.KEY_F;

@Singleton
public class ItemSearch extends Feature {

	/**
	 * An invisible marker to indicate guis where ItemSearch should be disabled
	 */
	public static final String marker = "§4§0§2§7§9§c§d§a§d§e§f§e§l§m§n§r";

	private final SwitchSetting inventory = SwitchSetting.create()
		.name("Inventar")
		.description("Ob das Inventar auch durchsucht werden soll.")
		.icon("chest");

	private final SwitchSetting dispenser = SwitchSetting.create()
		.name("Spender / Werfer")
		.description("Ob die Item-Suche auch bei Spendern / Werfern hinzugefügt werden soll.")
		.icon(Blocks.dispenser);

	private final SwitchSetting hopper = SwitchSetting.create()
		.name("Tricher")
		.description("Ob die Item-Suche auch bei Trichtern hinzugefügt werden soll.")
		.icon(Blocks.hopper);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Item-Suche")
		.description("Fügt eine Item-Suche innerhalb von Guis hinzu.")
		.icon("chest")
		.subSettings(dispenser, hopper, HeaderSetting.create(), inventory);

	public ModTextField searchField = null;
	private String previousSearch = "";

	@EventListener(triggerWhenDisabled = true)
	public void onGuiInit(GuiScreenEvent.GuiInitEvent event) {
		if (searchField != null)
			previousSearch = searchField.getText();

		searchField = null;
		if (!(event.gui instanceof GuiChest ||
			(dispenser.get() && event.gui instanceof GuiDispenser) ||
			(hopper.get() && event.gui instanceof GuiHopper)))
			return;

		String title = getGuiChestTitle();
		if (title.startsWith(marker) || title.startsWith("§6Profil") || title.startsWith("§6Lottoschein "))
			return;

		searchField = new ModTextField(0, mc().fontRendererObj, 82, 6, 83, mc().fontRendererObj.FONT_HEIGHT);
		searchField.setPlaceHolder("§oSuche...");
		searchField.setTextColor(0xffffff);
		searchField.setText(previousSearch);
		searchField.setEnableBackgroundDrawing(false);
	}

	@EventListener
	public void onKeyPress(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (searchField != null && Keyboard.getEventKeyState()) {
			if (searchField.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey())) {
				// Suppress inventory closing when keyBindInventory is pressed
				if (Keyboard.getEventKey() == mc().gameSettings.keyBindInventory.getKeyCode())
					event.cancel();
			}

			if (!searchField.isFocused() && Keyboard.getEventKey() == KEY_F && GuiScreen.isKeyComboCtrlA(KEY_A))
				searchField.setFocused(true);
		}
	}

	@EventListener
	public void onMousePress(GuiScreenEvent.MouseInputEvent.Post event) {
		if (searchField != null && Mouse.getEventButton() != -1) {
			int guiLeft = Reflection.get(event.gui, "guiLeft");
			int guiTop = Reflection.get(event.gui, "guiTop");

			int scale = new ScaledResolution(mc()).getScaleFactor();
			int x = Mouse.getEventX() / scale;
			int y = (mc().displayHeight - Mouse.getEventY()) / scale;

			x -= guiLeft;
			y -= guiTop;

			searchField.mouseClicked(x, y, Mouse.getEventButton());
		}
	}

	@EventListener
	public void onScreenDraw(DrawGuiContainerForegroundLayerEvent event) {
		if (searchField == null)
			return;

		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.translate(-1, -1, 300);

		// Draw search background
		mc().getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png"));
		event.container.drawTexturedModalRect(80, 4, 80, 4, 90, 12);

		searchField.drawTextBox();

		// Draw search
		String text = searchField.getText().toLowerCase();

		if (!text.isEmpty()) {
			for (Slot slot : event.container.inventorySlots.inventorySlots) {
				if (!inventory.get() && slot.inventory == player().inventory)
					break;

				if (shouldHide(slot.getStack(), text))
					GuiScreen.drawRect(slot.xDisplayPosition, slot.yDisplayPosition, slot.xDisplayPosition + 18, slot.yDisplayPosition + 18, 0xAA000000);
			}
		}

		GlStateManager.translate(1, 1, -300);
	}

	private boolean shouldHide(ItemStack stack, String text) {
		if (stack == null)
			return true;

		if (stack.getDisplayName().toLowerCase().replaceAll("§.", "").contains(text))
			return false;

		return !stack.getItem().getItemStackDisplayName(stack).toLowerCase().replaceAll("§.", "").contains(text);
	}

}