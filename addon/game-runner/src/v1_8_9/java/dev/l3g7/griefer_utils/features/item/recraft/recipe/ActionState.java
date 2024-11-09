/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.recipe;

import java.util.function.Function;
import java.util.function.Predicate;

enum ActionState {

	CATEGORY(e -> e.target.category + 11, e -> true),
	SHORTCUT(e -> e.shortcut, e -> true),
	PAGE(e -> e.target.page > e.current.page ? 53 : 52, e -> (e.target.page > e.current.page ? ++e.current.page : --e.current.page) == e.target.page),
	SLOT(e -> e.target.slot, e -> true),
	VARIANT(e -> e.target.variant > e.current.variant ? 50 : 48, e -> (e.target.variant > e.current.variant ? ++e.current.variant : --e.current.variant) == e.target.variant),
	CRAFT(e -> e.target.craftSlot, e -> true),

	BACK_TO_PAGE(e -> 45, e -> true);

	public final Function<ActionExecutor, Integer> slotFunction;
	public final Predicate<ActionExecutor> continuationPredicate;

	ActionState(Function<ActionExecutor, Integer> slotFunction, Predicate<ActionExecutor> continuationPredicate) {
		this.slotFunction = slotFunction;
		this.continuationPredicate = continuationPredicate;
	}

	public int getSlot(ActionExecutor executor) {
		return slotFunction.apply(executor);
	}

	public boolean canContinue(ActionExecutor executor) {
		return continuationPredicate.test(executor);
	}

}
