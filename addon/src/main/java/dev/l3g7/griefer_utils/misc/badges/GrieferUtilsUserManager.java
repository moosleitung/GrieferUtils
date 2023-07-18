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

package dev.l3g7.griefer_utils.misc.badges;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.UserSetGroupEvent;
import dev.l3g7.griefer_utils.misc.matrix.MatrixClient;
import io.netty.util.internal.ConcurrentSet;
import net.labymod.user.User;
import net.labymod.user.group.LabyGroup;
import net.minecraft.util.Util;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.labyMod;
import static net.minecraft.util.Util.EnumOS.OSX;
import static net.minecraft.util.Util.EnumOS.WINDOWS;

public class GrieferUtilsUserManager {

	private static final Map<UUID, LabyGroup> users = new ConcurrentHashMap<>();

	private static long lastRequest = 0;
	private static final Map<UUID, GrieferUtilsGroup> specialBadges = new HashMap<>();
	private static final Set<UUID> queuedUsers = new ConcurrentSet<>();

	public static void queueUser(UUID uuid) {
		queuedUsers.add(uuid);
	}

	public static void removeUser(UUID uuid) {
		user(uuid).setGroup(users.remove(uuid));
	}

	public static void clearUsers() {
		for (UUID uuid : users.keySet())
			removeUser(uuid);
	}

	private static User user(UUID uuid) {
		return labyMod().getUserManager().getUser(uuid);
	}

	@EventListener
	private static void onTick(TickEvent.ClientTickEvent event) {
		if (lastRequest + 2500 <= System.currentTimeMillis())
			requestQueuedUsers();
	}

	@EventListener
	private static void onSetGroup(UserSetGroupEvent event) {
		if (!users.containsKey(event.user.getUuid()) || event.group instanceof GrieferUtilsGroup)
			return;

		if (event.group == null)
			users.remove(event.user.getUuid());
		else
			users.put(event.user.getUuid(), event.group);

		event.setCanceled(true);
	}

	private static void requestQueuedUsers() {
		if (queuedUsers.isEmpty() || (Util.getOSType() != WINDOWS && Util.getOSType() != OSX))
			return;

		lastRequest = System.currentTimeMillis();

		Set<UUID> requestedUsers = new ConcurrentSet<>();
		requestedUsers.addAll(queuedUsers);
		queuedUsers.removeAll(requestedUsers);

		CompletableFuture.supplyAsync((Supplier<List<UUID>>) () -> MatrixClient.get().getOnlineUsers(requestedUsers)).thenAccept(uuids -> {
			for (UUID uuid : uuids) {
				users.put(uuid, user(uuid).getGroup());
				user(uuid).setGroup(specialBadges.getOrDefault(uuid, new GrieferUtilsGroup()));
			}
		});
	}

	static {
		IOUtil.read("https://grieferutils.l3g7.dev/v2/special_badges").asJsonObject(object -> {
			for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
				JsonObject groupData = entry.getValue().getAsJsonObject();
				String title = groupData.has("title") ? groupData.get("title").getAsString() : null;
				int color_with_labymod = 0xFFFFFF;
				int color_without_labymod = 0xFFFFFF;
				if (groupData.has("color")) {
					JsonElement color = groupData.get("color");
					if (color.isJsonPrimitive()) {
						color_with_labymod = color_without_labymod = color.getAsInt();
					} else {
						color_with_labymod = color.getAsJsonObject().get("with_labymod").getAsInt();
						color_without_labymod = color.getAsJsonObject().get("without_labymod").getAsInt();
					}
				}

				specialBadges.put(UUID.fromString(entry.getKey()), new GrieferUtilsGroup(color_with_labymod, color_without_labymod, title));
			}
		});
	}

}
