/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_menu.laby3.chat_line_util;

import net.minecraft.util.IChatComponent;

public interface GUChatLine {

	IChatComponent getModifiedComponent();
	IChatComponent getOriginalComponent();

}
