/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftAction;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeAction extends RecraftAction {

	public int category;
	public int page;
	public int slot;
	public int variant;
	public Ingredient result;
	public List<SizedIngredient> craftingIngredients;
	public int craftSlot;

	RecipeAction() {
		this(-1, 0, -1, 1, null, null, -1);
	}

	RecipeAction(int category, int page, int slot, int variant, Ingredient result, List<SizedIngredient> craftingIngredients, int craftSlot) {
		this.category = category;
		this.page = page;
		this.slot = slot;
		this.variant = variant;
		this.result = result;
		this.craftingIngredients = craftingIngredients;
		this.craftSlot = craftSlot;
	}

	public JsonElement toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("category", category);
		object.addProperty("page", page);
		object.addProperty("slot", slot);
		object.addProperty("variant", variant);
		object.addProperty("craftSlot", craftSlot);
		object.addProperty("result", result.toLong());
		JsonArray craftingJson = new JsonArray();

		for (SizedIngredient craftingIngredient : craftingIngredients)
			craftingJson.add(new JsonPrimitive(craftingIngredient.toLong()));

		object.add("crafting_ingredients", craftingJson);
		return object;
	}

	public static RecipeAction fromJson(JsonElement element) {
		if (element.isJsonPrimitive())
			return null;

		JsonObject object = element.getAsJsonObject();
		if (!object.has("page"))
			return null;

		int category = object.get("category").getAsInt();
		int page = object.get("page").getAsInt();
		int slot = object.get("slot").getAsInt();
		int typeIndex = object.get("variant").getAsInt();
		int craftSlot = object.get("craftSlot").getAsInt();
		Ingredient shortcut = Ingredient.fromLong(object.get("result").getAsLong());

		List<SizedIngredient> ingredients = new ArrayList<>();
		for (JsonElement jsonElement : object.getAsJsonArray("crafting_ingredients"))
			ingredients.add(SizedIngredient.fromLong(jsonElement.getAsLong()));

		return new RecipeAction(category, page, slot, typeIndex, shortcut, ingredients, craftSlot);
	}

	public RecipeAction copy() {
		return new RecipeAction(category, page, slot, variant, result, craftingIngredients, craftSlot);
	}

	public boolean isAvailable(ItemStack[] stacks, boolean checkForItemSaver) {
		return craftingIngredients.stream().allMatch(s -> s.isAvailable(stacks, checkForItemSaver));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RecipeAction that = (RecipeAction) o;
		return category == that.category && page == that.page && slot == that.slot && variant == that.variant;
	}

	@Override
	public String toString() {
		return String.format("%d %d %d %d %d", category, page, slot, variant, craftSlot);
	}

}
