/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server;

import com.mojang.util.UUIDTypeAdapter;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.PlayerKeyPair;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.LeaderboardRequest;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.LeaderboardRequest.LeaderboardData;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.StaticApiRequest;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.StaticApiRequest.StaticApiData;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.hive_mind.*;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.events.AccountSwitchEvent;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerJoinEvent;

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.HIGH;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static java.util.concurrent.TimeUnit.MINUTES;

@SuppressWarnings("UnusedReturnValue") // Callers may ignore Future<Void>s
public class GUServer {

	private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

	private static PlayerKeyPair currentKeyPair;
	private static StaticApiData staticApiData;

	// Authorization
	static {
		new Thread(() -> {
			renewToken();
			if (currentKeyPair != null) {
				SCHEDULER.scheduleAtFixedRate(() -> {
					if (isAvailable())
						new KeepAliveRequest().send();
				}, 0, 30, MINUTES);
			}
		}).start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				if (isAvailable())
					new LogoutRequest().send();
			} catch (Throwable ignored) {
			}
		}));
	}

	@EventListener(priority = HIGH)
	private void onAccountSwitch(AccountSwitchEvent event) {
		if (isAvailable())
			new LogoutRequest().send();

		renewToken();
	}

	static void renewToken() {
		if (mc().getSession().getUsername().equals(mc().getSession().getPlayerID()))
			currentKeyPair = null;
		else
			currentKeyPair = PlayerKeyPair.getPlayerKeyPair(mc().getSession().getToken()).join();
	}

	static String generateAuthHeader() {
		UUID uuid = UUIDTypeAdapter.fromString(mc().getSession().getPlayerID());

		try {
			return "v2 " + IOUtil.gson.toJson(new Request.AuthData(uuid, currentKeyPair)).replaceAll("[\r\n]", "");
		} catch (GeneralSecurityException t) {
			return null; // generateAuthHeader is not called on invalid sessions, this exception will never happen
		}
	}

	// Static API data
	@EventListener
	private static void onServerJoin(ServerJoinEvent event) {
		loadStaticApiData();
	}

	@OnStartupComplete
	static void loadStaticApiData() {
		if (staticApiData != null)
			return;

		CompletableFuture.supplyAsync(() -> {
			staticApiData = new StaticApiRequest().get();
			new StaticDataReceiveEvent(staticApiData).fire();
			return null;
		});
	}

	public static boolean isAvailable() {
		if (currentKeyPair != null)
			return true;

		renewToken();
		return currentKeyPair != null;
	}

	// Other requests
	public static CompletableFuture<List<UUID>> getOnlineUsers(Set<UUID> requestedUsers) {
		return CompletableFuture.supplyAsync(() -> {
			List<UUID> users = new OnlineUsersRequest(requestedUsers).send();
			if (users == null)
				users = Collections.emptyList();

			return users;
		});
	}

	public static CompletableFuture<Void> sendMobRemoverData(Citybuild citybuild, Long value) {
		return CompletableFuture.supplyAsync(() -> {
			new MobRemoverRequest(citybuild.getInternalName(), value).send();
			return null;
		});
	}

	public static CompletableFuture<Long> getMobRemoverData(Citybuild citybuild) {
		return CompletableFuture.supplyAsync(() -> new MobRemoverRequest(citybuild.getInternalName(), null).send());
	}

	public static CompletableFuture<LeaderboardData> getLeaderboardData() {
		return CompletableFuture.supplyAsync(() -> new LeaderboardRequest(false).get());
	}

	public static CompletableFuture<LeaderboardData> sendLeaderboardData(boolean flown) {
		return CompletableFuture.supplyAsync(() -> new LeaderboardRequest(flown).send());
	}

	public static CompletableFuture<Void> sendBlockOfTheDayBlock(String id, int damage, String event) {
		return CompletableFuture.supplyAsync(() -> {
			new BlockOfTheDayRequest.Block(id, damage, event).send();
			return null;
		});
	}

	public static CompletableFuture<Void> sendBlockOfTheDayReward(String type, int amount) {
		return CompletableFuture.supplyAsync(() -> {
			new BlockOfTheDayRequest.Reward(type, amount).send();
			return null;
		});
	}

}
