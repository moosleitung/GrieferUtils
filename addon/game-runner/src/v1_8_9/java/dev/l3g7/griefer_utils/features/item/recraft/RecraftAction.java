/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.TempItemSaverBridge;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Objects;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

public abstract class RecraftAction {

	public abstract JsonElement toJson();

	public static class Ingredient {

		public final int itemId;
		public final int compression;
		public final int meta;

		public static Ingredient fromItemStack(ItemStack stack) {
			if (stack == null)
				return null;

			if (!EnchantmentHelper.getEnchantments(stack).isEmpty())
				return null;

			// Check if the item is compressed
			int decompressedAmount = ItemUtil.getDecompressedAmount(stack);
			if (decompressedAmount == stack.stackSize)
				return new Ingredient(stack, 0);

			// Item is compressed, make sure it hasn't been placed yet
			int compressionLevel = ItemUtil.getCompressionLevel(stack);
			if (Math.pow(9, compressionLevel) * stack.stackSize != decompressedAmount)
				return null;

			return new Ingredient(stack, compressionLevel);
		}

		public Ingredient(ItemStack stack, int compression) {
			this(Item.getIdFromItem(stack.getItem()), stack.getMetadata(), compression);
		}

		public Ingredient(int itemId, int meta, int compression) {
			this.itemId = itemId;
			this.meta = meta;
			this.compression = compression;
		}

		public static boolean check(Ingredient ingredient, ItemStack stack) {
			if (ingredient == null)
				return true;

			return ingredient.equals(Ingredient.fromItemStack(stack));
		}

		public boolean itemEquals(Ingredient other) {
			if (other == null)
				return false;

			return (meta == other.meta || Recraft.ignoreSubIds)
				&& itemId == other.itemId;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Ingredient && equals((Ingredient) obj);
		}

		public boolean equals(Ingredient other) {
			if (!itemEquals(other))
				return false;

			return compression == other.compression;
		}

		public int getSlot(int[] excludedSlots, boolean checkForItemSaver) {
			ItemStack[] inv = player().inventory.mainInventory;
			invLoop:
			for (int i = 0; i < inv.length; i++) {
				if (!check(this, inv[i]))
					continue;

				if (checkForItemSaver && FileProvider.getBridge(TempItemSaverBridge.class).isProtected(inv[i]))
					continue;

				for (int excludedSlot : excludedSlots)
					if (i == excludedSlot)
						continue invLoop;

				return i;
			}

			return -1;
		}

		public long toLong() {
			long result = compression;
			// 10000 is used instead of real bit shifting to maintain readability
			result += (long) meta * 10000;
			result += (long) itemId * 10000_0000;
			return result;
		}

		public static Ingredient fromLong(long value) {
			int compression = (int) (value % 10000);
			int meta = (int) (value / 10000 % 10000);
			int itemId = (int) (value / 10000_0000 % 10000);
			return new Ingredient(itemId, meta, compression);
		}

		public String toString() {
			return String.format("%d:%d (%d)", itemId, meta, compression);
		}

		@Override
		public int hashCode() {
			return Objects.hash(itemId, compression, meta);
		}

	}

}
