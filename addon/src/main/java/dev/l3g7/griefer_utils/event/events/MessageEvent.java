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

package dev.l3g7.griefer_utils.event.events;

import dev.l3g7.griefer_utils.core.event_bus.Event;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.main.LabyMod;
import net.labymod.utils.manager.TagManager;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.labyMod;

/**
 * A forge event for message processing.
 */
public class MessageEvent extends Event {

	public static class MessageModifyEvent extends MessageEvent {

		public final IChatComponent original;
		public IChatComponent message;

		public MessageModifyEvent(IChatComponent original, IChatComponent message) {
			this.original = original;
			this.message = message.createCopy();
		}

		@Mixin(value = TagManager.class, remap = false)
		private static class MixinTagManager {

			private static IChatComponent originalMessage;

			@ModifyVariable(method = "tagComponent", at = @At("HEAD"), ordinal = 0, argsOnly = true)
			private static Object injectTagComponent(Object value) {
				originalMessage = (IChatComponent) value;
				return value;
			}

			@ModifyVariable(method = "tagComponent", at = @At(value = "INVOKE", target = "Lnet/labymod/utils/manager/TagManager;getConfigManager()Lnet/labymod/utils/manager/ConfigManager;", shift = At.Shift.BEFORE, ordinal = 0), ordinal = 0, argsOnly = true)
			private static Object injectTagComponentReturn(Object value) {
				MessageModifyEvent event = new MessageModifyEvent(originalMessage, (IChatComponent) value);
				event.fire();
				return event.message;
			}

		}

	}

	public static class MessageModifiedEvent extends MessageEvent {

		public final IChatComponent component;

		public MessageModifiedEvent(IChatComponent component) {
			this.component = component;
		}

		@Mixin(value = GuiChatAdapter.class, remap = false)
		private static class MixinGuiChatAdapter {

			@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/MessageData;getFilter()Lnet/labymod/ingamechat/tools/filter/Filters$Filter;"))
			public void postMessageModifiedEvent(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
				new MessageEvent.MessageModifiedEvent(component).fire();
			}

		}

	}

	/**
	 * A forge event for LabyMod's {@link net.labymod.api.events.MessageSendEvent}.
	 */
	public static class MessageSendEvent extends MessageEvent {

		public static boolean post(String message) {
			if (!new MessageSendEvent(message).fire().isCanceled()) {
				for (net.labymod.api.events.MessageSendEvent lmEvent : labyMod().getEventManager().getMessageSend())
					if (lmEvent.onSend(message))
						return true;

				return false;
			}

			return true;
		}

		public final String message;

		private MessageSendEvent(String message) {
			this.message = message;
		}

		@OnEnable
		private static void register() {
			LabyMod.getInstance().getEventManager().register((net.labymod.api.events.MessageSendEvent) s -> new MessageSendEvent(s).fire().isCanceled());
		}

	}

	public static class MessageAboutToBeSentEvent extends MessageEvent {

		public final String message;

		public MessageAboutToBeSentEvent(String message) {
			this.message = message;
		}

		@Mixin(EntityPlayerSP.class)
		private static class MixinEntityPlayerSP {

			@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
			public void injectSendChatMessage(String message, CallbackInfo ci) {
				if (new MessageEvent.MessageAboutToBeSentEvent(message).fire().isCanceled())
					ci.cancel();
			}

		}

	}

	public static class MessageReceiveEvent extends MessageEvent {

		public final IChatComponent message;
		public final byte type;

		public MessageReceiveEvent(IChatComponent message, byte type) {
			this.message = message;
			this.type = type;
		}

		@EventListener
		private static void onPacketReceive(PacketEvent.PacketReceiveEvent<S02PacketChat> event) {
			if (new MessageReceiveEvent(event.packet.getChatComponent(), event.packet.getType()).fire().isCanceled())
				event.cancel();
		}

	}

}
