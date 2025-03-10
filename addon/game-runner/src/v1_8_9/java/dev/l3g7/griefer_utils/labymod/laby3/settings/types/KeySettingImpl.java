/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.core.events.InputEvent.KeyInputEvent;
import dev.l3g7.griefer_utils.core.events.InputEvent.MouseInputEvent;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import net.labymod.gui.elements.ModTextField;
import net.labymod.main.LabyMod;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.StringElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class KeySettingImpl extends ControlElement implements Laby3Setting<KeySetting, Set<Integer>>, KeySetting {

	private final ModTextField previewField;
	private final List<Consumer<Boolean>> pressCallbacks = new ArrayList<>();
	private boolean pressed;
	private boolean triggersInContainers = false;

	private final ExtendedStorage<Set<Integer>> storage = new ExtendedStorage<>(
		values -> {
			JsonArray array = new JsonArray();
			values.stream().map(JsonPrimitive::new).forEach(array::add);
			return array;
		},
		elem -> new LinkedHashSet<>(
			StreamSupport.stream(elem.getAsJsonArray().spliterator(), false)
				.map(JsonElement::getAsInt)
				.collect(Collectors.toList())),
		new TreeSet<>()
	);

	public KeySettingImpl() {
		super("§cNo name set", null);
		previewField = new ModTextField(-2, mc.fontRendererObj, 0, 0, getObjectWidth() - 5, 20);
		previewField.setMaxStringLength(500);
		previewField.setText("NONE");
		previewField.setCursorPositionEnd();
		previewField.setFocused(false);
		setSettingEnabled(true);
		EventRegisterer.register(this);
	}

	@Override
	public ExtendedStorage<Set<Integer>> getStorage() {
		return storage;
	}

	@Override
	public KeySetting set(Set<Integer> value) {
		previewField.setText(formatKeys(value));
		return Laby3Setting.super.set(value);
	}

	public KeySetting pressCallback(Consumer<Boolean> callback) {
		pressCallbacks.add(callback);
		return this;
	}

	@Override
	public KeySetting placeholder(String placeholder) {
		previewField.setPlaceHolder(placeholder);
		return this;
	}

	public KeySetting triggersInContainers() {
		triggersInContainers = true;
		return this;
	}

	@EventListener
	public void onGuiKeyPress(GuiScreenEvent.KeyboardInputEvent.Post event) {
		if (!Keyboard.isRepeatEvent() && triggersInContainers)
			onPress(Keyboard.getEventKey());
	}

	@EventListener
	public void onGuiMousePress(GuiScreenEvent.MouseInputEvent.Post event) {
		if (triggersInContainers)
			onPress(-Mouse.getEventButton());
	}

	@EventListener
	public void onKeyPress(KeyInputEvent event) {
		if (!Keyboard.isRepeatEvent())
			onPress(Keyboard.getEventKey());
	}

	@EventListener
	private void onMousePress(MouseInputEvent event) {
		if (Mouse.getEventButton() != -1)
			onPress(-Mouse.getEventButton());
	}

	private void onPress(int code) {
		if (!get().contains(code))
			return;

		pressed = isPressed();
		pressCallbacks.forEach(c -> c.accept(pressed));
	}

	public boolean isPressed() {
		return get().stream().allMatch(i -> i > 0 ? Keyboard.isKeyDown(i) : Mouse.isButtonDown(-i));
	}

	/**
	 * Draws the key setting.
	 */
	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		if (previewField == null)
			return;

		previewField.xPosition = maxX - getObjectWidth() + 3;
		previewField.yPosition = y + 1;
		previewField.drawTextBox();
		LabyMod.getInstance().getDrawUtils().drawRectangle(x - 1, y, x, maxY, 0x78787878);
	}

	/**
	 * Intercepts clicks into the preview field and opens a {@link KeySelectionGui}.
	 */
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!previewField.mouseClicked(mouseX, mouseY, mouseButton))
			return;

		previewField.setFocused(false);
		mc.displayGuiScreen(new KeySelectionGui());
	}

	@Override
	public int getObjectWidth() {
		return 85;
	}

	/**
	 * Based on LabyMod's {@link StringElement.ExpandedStringElementGui}.
	 */
	private class KeySelectionGui extends GuiScreen {

		private final Set<Integer> selectedKeys = new LinkedHashSet<>();
		private final Set<Integer> pressedKeys = new LinkedHashSet<>();
		private final GuiScreen backgroundScreen = Minecraft.getMinecraft().currentScreen;
		private ModTextField selectionField;

		@Override
		public void initGui() {
			super.initGui();
			// init background screen
			backgroundScreen.width = width;
			backgroundScreen.height = height;
			if (backgroundScreen instanceof LabyModModuleEditorGui)
				PreviewRenderer.getInstance().init(KeySelectionGui.class);

			// init selection field
			selectionField = new ModTextField(0, mc.fontRendererObj, width / 2 - 150, height / 2 - 30, 300, 20);
			selectionField.setMaxStringLength(Integer.MAX_VALUE);
			selectionField.setFocused(true);
			selectionField.setText(KeySettingImpl.this.previewField.getText());
			selectionField.setCursorPositionEnd();

			// init buttons
			buttonList.add(new GuiButton(1, width / 2 - 110, height / 2 + 10, 100, 20, "Zurücksetzen"));
			buttonList.add(new GuiButton(2, width / 2 + 10, height / 2 + 10, 100, 20, "Speichern"));
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			backgroundScreen.drawScreen(mouseX, mouseY, partialTicks);
			drawRect(0, 0, width, height, Integer.MIN_VALUE);
			selectionField.drawTextBox();
			super.drawScreen(mouseX, mouseY, partialTicks);
		}

		@Override
		public void handleKeyboardInput() {
			super.handleKeyboardInput();

			int keyCode = Keyboard.getEventKey();

			// check ESC press
			if (keyCode == 1) {
				close();
				return;
			}

			// process selection
			if (!Keyboard.getEventKeyState()) {
				pressedKeys.remove(keyCode);
				return;
			}

			// If no keys have been pressed before, begin new input
			if (pressedKeys.isEmpty())
				selectedKeys.clear();

			pressedKeys.add(keyCode);

			// Add button to keys
			selectedKeys.add(keyCode);
			selectionField.setText(formatKeys(selectedKeys));
			selectionField.setCursorPositionEnd();
		}

		@Override
		public void handleMouseInput() {
			super.handleMouseInput();

			int keyCode = -Mouse.getEventButton();

			// only capture special buttons
			if (keyCode == 0 || keyCode == -1)
				return;


			// process selection
			if (!Mouse.getEventButtonState()) {
				pressedKeys.remove(keyCode);
				return;
			}

			// If no keys have been pressed before, begin new input
			if (pressedKeys.isEmpty())
				selectedKeys.clear();

			pressedKeys.add(keyCode);

			// Add button to keys
			selectedKeys.add(keyCode);
			selectionField.setText(formatKeys(selectedKeys));
			selectionField.setCursorPositionEnd();
		}

		@Override
		public void updateScreen() {
			backgroundScreen.updateScreen();
		}

		@Override
		protected void actionPerformed(GuiButton button) {
			if (button.id == 1) {
				// Reset button
				selectedKeys.clear();
				pressedKeys.clear();
				selectionField.setText(formatKeys(selectedKeys));
				selectionField.setCursorPositionEnd();
			} else if (button.id == 2)
				// Save button
				close();
		}

		/**
		 * Closes the selection gui and updates the parent setting.
		 */
		private void close() {
			EventRegisterer.unregister(this);
			Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);

			// Only trigger callback if something changed
			if (selectionField.getText().equals(previewField.getText()))
				return;
			set(selectedKeys);
		}
	}

	public static String formatKeys(Collection<Integer> keys) {
		if (keys.isEmpty())
			return "NONE";

		return keys.stream()
			.map(i -> i > 0 ? Keyboard.getKeyName(i) : "MOUSE " + -i)
			.collect(Collectors.joining(" + "));
	}

}