/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.recipe;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.item.recraft.Recraft;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftAction.Ingredient;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftRecording;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C0EPacketClickWindow;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static dev.l3g7.griefer_utils.features.item.recraft.RecraftRecordingCore.RecordingMode.RECIPE;

public class RecipeRecorder {

	private static RecraftRecording recording = Recraft.tempRecording;
	private static GuiScreen previousScreen = null;
	private static boolean addedIcon = false;
	private static boolean isMenuOpen = false;
	private static boolean executedCommand = false;

	private static RecipeAction action;

	public static void startRecording(RecraftRecording recording) {
		if (!ServerCheck.isOnCitybuild()) {
			labyBridge.notify("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild gestartet werden.");
			return;
		}

		RecipeRecorder.recording = recording;
		previousScreen = mc().currentScreen;
		addedIcon = false;
		executedCommand = true;
		send("/rezepte");
	}

	@EventListener
	private static void onMessageSend(PacketSendEvent<C01PacketChatMessage> event) {
		String lowerMsg = event.packet.getMessage().toLowerCase();
		if (!Recraft.isPlaying() && (lowerMsg.equals("/rezepte") || lowerMsg.startsWith("/rezepte ")))
			executedCommand = true;
	}

	@EventListener
	private static void onGuiOpen(GuiOpenEvent<?> event) {
		if (Recraft.isPlaying())
			return;

		if (event.gui instanceof GuiChest) {
			if (executedCommand) {
				isMenuOpen = true;
				recording.actions().clear();
				recording.mode().set(RECIPE);
				action = new RecipeAction();
				executedCommand = false;
			}
			return;
		}

		recording = Recraft.tempRecording;
		isMenuOpen = false;
		if (previousScreen == null || previousScreen == event.gui)
			return;

		event.cancel();
		mc().displayGuiScreen(previousScreen);
		previousScreen = null;
	}

	@EventListener
	private static void onSendClick(PacketSendEvent<C0EPacketClickWindow> event) {
		if (!isMenuOpen)
			return;

		C0EPacketClickWindow packet = event.packet;
		if (packet.getClickedItem() == null || packet.getClickedItem().getDisplayName().equals("§7") || packet.getMode() == 3)
			return;

		int slot = packet.getSlotId();

		String title = MinecraftUtil.getGuiChestTitle();
		if (action.category == -1) {
			if (!title.startsWith("§6Custom-Kategorien"))
				return;

			if (slot > 26) // Player clicked into inventory
				return;

			if (slot == 11)
				action.category = 0;
			else if (slot == 12)
				action.category = 1;
			else if (recording != Recraft.tempRecording) {
				labyBridge.notify("§cFehler \u26A0", "§cDiese Kategorie ist nicht implementiert!");
				mc().displayGuiScreen(null);
			}

			return;
		}

		if (slot == 45)  { // Back button
			if (action.slot != -1) {
				action.slot = -1;
				if (action.category == 0)
					action.page = -1;
			} else if (recording.actions().isEmpty()) {
				action.page = action.category = -1;
			} else if (recording != Recraft.tempRecording) {
				labyBridge.notify("§cFehler \u26A0", "§cNachfolgende Aktionen müssen in derselben Kategorie sein!");
				recording = Recraft.tempRecording;
				mc().displayGuiScreen(previousScreen);
				previousScreen = null;
				event.cancel();
			}

			return;
		}

		boolean crafted = "§7Klicke, um dieses Rezept herzustellen.".equals(ItemUtil.getLoreAtIndex(packet.getClickedItem(), 0));

		if (!crafted && action.slot == -1) {
			if (slot < 45)
				action.slot = slot;
			else if (slot == 52)
				action.page = Math.min(0, action.page - 1);
			else if (slot == 53)
				action.page++;
			else if (recording != Recraft.tempRecording)
				labyBridge.notify("§eWarnung \u26A0", "§eBitte wähle das Item via GrieferGames' Gui aus!");

			return;
		}

		if (!crafted)
			return;

		ItemStack variantItem = player().openContainer.getSlot(49).getStack();
		String variantString = variantItem.getDisplayName();
		if (!variantString.equals("§7"))
			action.variant = Integer.parseInt(variantString.substring(variantString.indexOf(' ') + 1));
		else
			action.variant = 1;

		ItemStack targetStack = player().openContainer.getSlot(25).getStack().copy();
		action.result = Ingredient.fromItemStack(targetStack);

		if (!addedIcon) {
			targetStack.stackSize = ItemUtil.getCompressionLevel(targetStack);
			recording.setIcon(targetStack);
			addedIcon = true;
		}

		Ingredient[] ingredients = new Ingredient[9];
		for (int i = 0; i < 9; i++) {
			ItemStack stack = player().openContainer.getSlot(10 + i % 3 + i / 3 * 9).getStack();
			if (stack != null && !stack.getDisplayName().equals("§7"))
				ingredients[i] = Ingredient.fromItemStack(stack);
		}

		if (packet.getMode() == 1)
			slot = -slot;

		action.craftingIngredients = SizedIngredient.fromIngredients(ingredients);
		action.craftSlot = slot;

		if (!recording.actions().contains(action)) {
			recording.actions().add(action);
			action = action.copy();
		}
	}

}