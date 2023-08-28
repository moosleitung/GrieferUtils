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

package dev.l3g7.griefer_utils.features.chat.auto_unnick;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.WebDataReceiveEvent;

public class PrefixFinder {
	public static String[] prefixes = null;

	private final String colorOnlyText;
	private final String originalText;

	public PrefixFinder(String rank, String name) {

		if (rank.charAt(1) != name.charAt(1) || rank.contains("+")) // RW prefix || Streamer/YT +
			originalText = trim(name);
		else
			originalText = trim(rank.length() > name.length() ? rank : name);

		this.colorOnlyText = originalText.replaceAll("§[^0-9a-f]", "");
	}

	public String getPrefix() {
		// Single prefix (<= 2 because of §l / §k)
		if (originalText.lastIndexOf('§') <= 2)
			return originalText.substring(0, originalText.lastIndexOf('§') + 2)
				.replace("§", "");

		char firstCode = colorOnlyText.charAt(1);

		// Double prefix
		if (firstCode == getFormattingAt(1)
			&& firstCode == getFormattingAt(4)) {
			String secondString = String.valueOf(getFormattingAt(2));
			String firstString = String.valueOf(firstCode);
			return firstString + firstString + secondString + secondString;
		}

		// Prefixes couldn't be loaded
		if (prefixes == null)
			return "4";

		for (String currentPrefix : prefixes) {
			char[] chars = currentPrefix.toCharArray();
			boolean isPrefix = true;

			// At least 5 chars have to be compared to be sure (Lucky and Halloween 1 both start with 66ee)
			for (int i = 0; i < 5; i++)
				isPrefix &= (chars[i % chars.length] == getFormattingAt(i));

			if (isPrefix)
				return currentPrefix;
		}

		return "4";
	}

	// Removes all §r at the start of the text
	private String trim(String text) {
		while (text.startsWith("§r"))
			text = text.substring(2);

		return text;
	}

	// Returns the formatting code of the char at the given index
	public Character getFormattingAt(int index) {
		Character formattingCode = null;

		char[] chars = colorOnlyText.toCharArray();

		int currentIndex = 0;
		for (int i = 0; currentIndex <= index; ) {
			if (chars[i++] == '§')
				formattingCode = chars[i++];
			else
				currentIndex++;
		}

		return formattingCode;
	}

	@EventListener
	private static void onWebData(WebDataReceiveEvent event) {
		prefixes = event.data.repeatingPrefixes;
	}

}