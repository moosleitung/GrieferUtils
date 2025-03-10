/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.balance_info.inventory_value.laby3;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.chat.Calculator;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiBigChest;
import dev.l3g7.griefer_utils.features.widgets.Laby3Widget;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.features.widgets.balance_info.inventory_value.laby3.InventoryValue.auto;

public class InventoryValueWidget {

	@Singleton
	@ExclusiveTo(LABY_3)
	public static class InventoryValue extends Laby3Widget {

		private static final Pattern VALUE_PATTERN = Pattern.compile("\\b([\\d,.k]+)\\b");
		public static String entryKey = "modules.inventory_value.entries";
		private GuiScreen previousScreen = null;

		@EventListener
		private void onAddItem(WindowClickEvent event) {
			if (previousScreen == null || event.itemStack == null)
				return;

			mc.displayGuiScreen(new EnterItemValueGui(value -> addItem(event.itemStack, value), previousScreen, getValue(event.itemStack)));
			previousScreen = null;
			event.cancel();
		}

		@Override
		public void fillSubSettings(List<SettingsElement> list) {
			super.fillSubSettings(list);

			if (Config.has(entryKey)) {
				JsonObject entries = Config.get(entryKey).getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : entries.entrySet())
					list.add(new ItemDisplaySetting(entry.getKey(), entry.getValue().getAsLong()));
			}

			list.add(new EntryAddSetting("Item hinzufügen")
				.callback(() -> {
					if (mc.thePlayer == null) {
						LabyBridge.labyBridge.notify("§e§lFehler ⚠", "§eHinzufügen von Items ist nur Ingame möglich!", 5000);
						return;
					}

					previousScreen = mc.currentScreen;
					display(Constants.ADDON_PREFIX + "Bitte klicke das Item an, das du hinzufügen möchtest.");
					mc.displayGuiScreen(null);
				}));
		}

		@Override
		public String[] getKeys() {
			if (mc.currentScreen instanceof GuiInventory
				|| mc.currentScreen instanceof GuiBigChest
				|| !(mc.currentScreen instanceof GuiContainer)
				&& !(mc.currentScreen instanceof LabyModModuleEditorGui))
				return new String[] {"Eigenes Inventar"};

			return getDefaultKeys();
		}

		@Override
		public String[] getDefaultKeys() {
			return new String[] {"Eigenes Inventar", "Geöffnetes Inventar"};
		}

		@Override
		public String[] getValues() {
			if (player() == null)
				return getDefaultValues();

			if (rawBooleanElement == null)
				createSettingElement();

			String invValue = getValue(Arrays.asList(player().inventory.mainInventory));
			if (!(mc.currentScreen instanceof GuiContainer) || mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiBigChest)
				return mc.currentScreen instanceof LabyModModuleEditorGui ? new String[] {invValue, "0$"} : new String[] {invValue};

			List<Slot> slots = ((GuiContainer) mc.currentScreen).inventorySlots.inventorySlots;
			slots = slots.subList(0, slots.size() - 9 * 4);
			return new String[] {invValue, getValue(slots.stream().map(Slot::getStack).collect(Collectors.toList()))};
		}

		@Override
		public String[] getDefaultValues() {
			return new String[] {"0$", "0$"};
		}

		private void addItem(ItemStack stack, long value) {
			ItemStack is = stack.copy();
			is.stackSize = 1;

			ItemDisplaySetting setting = getSetting(stack);
			if (setting != null) {
				setting.value = value;
				onChange();
				return;
			}

			List<SettingsElement> settings = rawBooleanElement.getSubSettings().getElements();
			settings.add(settings.size() - 1, new ItemDisplaySetting(is, value));
			onChange();
		}

		private static ItemDisplaySetting getSetting(ItemStack stack) {
			if (stack == null)
				return null;

			ItemStack is = stack.copy();
			is.stackSize = 1;

			ListIterator<SettingsElement> iterator = FileProvider.getSingleton(InventoryValue.class).rawBooleanElement.getSubSettings().getElements().listIterator();
			String nbt = ItemUtil.serializeNBT(is);

			while (iterator.hasNext()) {
				SettingsElement element = iterator.next();

				if (element instanceof ItemDisplaySetting) {
					ItemDisplaySetting ids = (ItemDisplaySetting) element;
					if (nbt.equals(ItemUtil.serializeNBT(ids.getStack())))
						return ids;
				}

				if (element instanceof EntryAddSetting)
					break;
			}

			return null;
		}

		public static void onChange() {
			if (mc.currentScreen != null)
				mc.currentScreen.initGui();

			JsonObject object = new JsonObject();
			for (SettingsElement element : FileProvider.getSingleton(InventoryValue.class).rawBooleanElement.getSubSettings().getElements()) {
				if (!(element instanceof ItemDisplaySetting))
					continue;

				ItemDisplaySetting ids = (ItemDisplaySetting) element;
				object.addProperty(ItemUtil.serializeNBT(ids.getStack()), ids.value);
			}

			Config.set(entryKey, object);
			Config.save();
		}

		private static long getValue(ItemStack stack) {
			if (stack == null)
				return -1;

			List<String> lore = ItemUtil.getLore(stack);
			if (lore.size() < 3)
				return -1;

			if (!lore.get(lore.size() - 1).startsWith("§7Signiert von"))
				return -1;

			for(String string : new String[] {lore.get(lore.size() - 2), stack.getDisplayName()}) {
				if (string.startsWith("§7Signiert von"))
					continue;

				string = string.toLowerCase()
					.replaceAll("§.", "")
					.replaceAll("(?<=\\d)\\.(\\d{3})", "$1")
					.replaceAll(" ?mio", "m")
					.replace("m", "kk");

				Matcher matcher = VALUE_PATTERN.matcher(string.replaceAll("§.", ""));
				if (matcher.find()) {
					String result = matcher.group(1);
					if (!matcher.find()) { // Cancel if multiple numbers are found
						try {
							double value = Calculator.calculate(result, false);
							if (Double.isNaN(value))
								return -1;

							return (long) value;
						} catch (NumberFormatException ignored) {}
					}
				}

			}

			// No value was found
			return -1;
		}

		private static String getValue(List<ItemStack> itemStacks) {
			long value = 0;

			for (ItemStack itemStack : itemStacks) {
				ItemDisplaySetting ids = getSetting(itemStack);

				if (ids != null) {
					value += ids.value * itemStack.stackSize;
				} else if (auto.get()) {
					long itemValue = getValue(itemStack);
					if (itemValue != -1)
						value += itemValue * itemStack.stackSize;
				}
			}

			return Constants.DECIMAL_FORMAT_98.format(value) + "$";
		}

	}
}
