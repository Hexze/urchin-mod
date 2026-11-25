//? if = 1.8.9 {
/*package gg.urchin.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import gg.urchin.client.tags.TagLabels;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    // Hooks the stored string rather than getDisplayName, which Sarcio's name_tag_text skips.
    @ModifyVariable(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At("STORE"), ordinal = 0)
    private String urchin$labelTaggedPlayer(String name, @Local(argsOnly = true) LivingEntity entity) {
        return entity instanceof PlayerEntity player ? TagLabels.nametag(player, name) : name;
    }
}
*///?}
