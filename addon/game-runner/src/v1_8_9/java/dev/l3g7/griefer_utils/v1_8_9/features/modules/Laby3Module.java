/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.features.modules;

import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.event.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.api.event.event_bus.Disableable;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.laby3.settings.types.SwitchSettingImpl;
import dev.l3g7.griefer_utils.settings.SettingLoader;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.ingamegui.ModuleCategoryRegistry;
import net.labymod.ingamegui.enums.EnumDisplayType;
import net.labymod.ingamegui.enums.EnumModuleFormatting;
import net.labymod.ingamegui.moduletypes.SimpleModule;
import net.labymod.ingamegui.moduletypes.SimpleTextModule;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.CategoryModuleEditorElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_3;

@ExclusiveTo(LABY_3)
public abstract class Laby3Module extends SimpleTextModule implements Disableable {

	public static final ModuleCategory CATEGORY = new ModuleCategory(Constants.ADDON_NAME, true, null) {
		@Override
		public void createCategoryElement() {
			rawCategoryElement = new CategoryModuleEditorElement(Constants.ADDON_NAME, new IconData("griefer_utils/icons/icon.png")) {
				// Fix module count
				public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
					this.mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

					int absoluteY = y + 7;
					DrawUtils draw = LabyMod.getInstance().getDrawUtils();
					draw.drawRectangle(x, y, maxX, maxY, ModColor.toRGB(200, 200, 200, mouseOver ? 50 : 30));
					int imageSize = maxY - y;
					Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/icon.png"));
					LabyMod.getInstance().getDrawUtils().drawTexture(x + 2, y + 2, 256, 256, 18, 18);

					draw.drawString(getDisplayName(), x + imageSize + 5, absoluteY);
					int totalSubCount = 0;
					int enabledCount = 0;

					for (SettingsElement element : getSubSettings().getElements()) {
						if (element instanceof BooleanElement) {
							++totalSubCount;
							if (((BooleanElement) element).getCurrentValue())
								++enabledCount;
						}
					}

					draw.drawRightString(enabledCount + "§7/§f" + totalSubCount, maxX - 5, absoluteY);
				}
			};

			// Populate subSettings
			for (net.labymod.ingamegui.Module module : net.labymod.ingamegui.Module.getModules())
				if (module.getCategory() != null && module.getCategory().equals(this))
					this.rawCategoryElement.getSubSettings().add(module.getBooleanElement().custom("An", "Aus"));

			// Inject headers
			SimpleModule stylingModule = new SimpleModule(){
				public String getDisplayName() { return ""; }
				public String getDisplayValue() { return ""; }
				public String getDefaultValue() { return ""; }
				public String getSettingName() { return ""; }
				public String getDescription() { return null; }
				public void loadSettings() {}
				public int getSortingId() { return 0; }
				public IconData getIconData() { return null; }
				public boolean isEnabled(EnumDisplayType displayType) { return true; }
			};

			List<SettingsElement> elems = CATEGORY.getCategoryElement().getSubSettings().getElements();

			int offset = 0;
			elems.add(offset++, (SettingsElement) HeaderSetting.create().entryHeight(8));
			elems.add(offset++, (SettingsElement) HeaderSetting.create("§r§l" + Constants.ADDON_NAME).scale(1.3));
			elems.add(offset++, (SettingsElement) HeaderSetting.create("Geld-Informationen"));
			elems.add(3 + offset++, (SettingsElement) HeaderSetting.create("Geld-Statistiken"));
			elems.add(6 + offset++, (SettingsElement) HeaderSetting.create("Orb-Statistiken"));
			elems.add(8 + offset++, (SettingsElement) HeaderSetting.create("Countdowns"));
			elems.add(11 + offset, (SettingsElement) HeaderSetting.create("Misc"));

			for (SettingsElement elem : elems)
				if (((ControlElement) elem).getModule() == null)
					Reflection.set(elem, "module", stylingModule);
		}
	};

	@OnEnable
	public static void register() {
		ModuleCategoryRegistry.loadCategory(CATEGORY);

		FileProvider.getClassesWithSuperClass(Laby3Module.class).stream()
			.map(meta -> (Laby3Module) FileProvider.getSingleton(meta.load()))
			.map(Laby3Module::initModule)
			.sorted((a, b) -> a.getComparisonName().compareToIgnoreCase(b.getComparisonName()))
			.forEach(LabyMod.getInstance().getLabyModAPI()::registerModule);
	}

	@OnStartupComplete
	public static void fixModules() {
		// Fix bug where category doesn't appear in module gui
		if (!ModuleCategoryRegistry.getCategories().contains(CATEGORY))
			ModuleCategoryRegistry.getCategories().add(CATEGORY);

		if (!ModuleCategoryRegistry.ADDON_CATEGORY_LIST.contains(CATEGORY))
			ModuleCategoryRegistry.ADDON_CATEGORY_LIST.add(CATEGORY);

		// Fix the modules' settings not having descriptions
		for (net.labymod.ingamegui.Module module : Laby3Module.getModules())
			if (module.getCategory() == CATEGORY)
				module.getBooleanElement().setDescriptionText(module.getDescription());
	}

	public SwitchSettingImpl mainElement;
	private String configKey;

	private Laby3Module initModule() {
		SettingLoader.MainElementData data = SettingLoader.initMainElement(this, "modules");
		mainElement = (SwitchSettingImpl) data.mainElement;
		configKey = data.configKey;
		return this;
	}

	protected Text toText(String text) {
		return new Text(text, valueColor, bold, italic, underline);
	}

	public String getComparisonName() {
		return getClass().getPackage().getName() + getControlName();
	}

	public String getControlName() { return mainElement.getDisplayName(); }

	public String[] getKeys() { return getDefaultKeys(); }
	public String[] getDefaultKeys() { return new String[]{ mainElement.getDisplayName().replace("\n", "")}; }

	public IconData getIconData() { return mainElement.getIconData(); }
	public String getSettingName() { return configKey; }
	public String getDescription() { return mainElement.getDescriptionText(); }
	public boolean isShown() { return !LabyMod.getInstance().isInGame() || ServerCheck.isOnGrieferGames(); }
	public boolean isEnabled() { return mainElement.get(); }

	public void loadSettings() {}
	public int getSortingId() { return 0; }
	public ModuleCategory getCategory() { return CATEGORY; }
	public EnumModuleFormatting getDisplayFormatting() { return super.getDisplayFormatting(); }

	public void fillSubSettings(List<SettingsElement> list) {
		list.add((SettingsElement) HeaderSetting.create().entryHeight(8));
		list.add((SettingsElement) HeaderSetting.create("§r§l" + Constants.ADDON_NAME).scale(1.3));
		list.add((SettingsElement) HeaderSetting.create(getControlName().replace("\n", "")));
		super.fillSubSettings(list);
		list.add((SettingsElement) HeaderSetting.create());

		getBooleanElement().addCallback(mainElement::set);
		mainElement.callback(b -> Reflection.set(rawBooleanElement, "currentValue", b));
		Reflection.set(rawBooleanElement, "currentValue", mainElement.get());

		List<SettingsElement> settings = mainElement.getSubSettings().getElements();
		if (!settings.isEmpty())
			list.addAll(settings.subList(4, settings.size()));
	}

}