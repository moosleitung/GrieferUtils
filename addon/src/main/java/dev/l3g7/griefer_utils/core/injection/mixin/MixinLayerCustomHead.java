package dev.l3g7.griefer_utils.core.injection.mixin;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.player.ShowHatItems;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Mixin(LayerCustomHead.class)
public class MixinLayerCustomHead {

	@SuppressWarnings("deprecation")
	@Inject(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;popMatrix()V", shift = At.Shift.BEFORE))
	public void injectDoRenderLayer(EntityLivingBase entity, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale, CallbackInfo ci) {
		if (!FileProvider.getSingleton(ShowHatItems.class).isEnabled())
			return;

		ItemStack itemstack = entity.getCurrentArmor(3);

		if (itemstack.getItem() instanceof ItemBlock || itemstack.getItem() == Items.skull || itemstack.getItem() instanceof ItemArmor)
			return;

		GlStateManager.translate(0.0f, -0.75, 0.275f);
		GlStateManager.scale(0.625f, -0.625f, -0.625f);

		mc().getItemRenderer().renderItem(entity, itemstack, ItemCameraTransforms.TransformType.NONE);
	}

}
