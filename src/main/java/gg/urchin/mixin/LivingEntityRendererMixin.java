//? if = 1.8.9 {
/*package gg.urchin.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import gg.urchin.client.tags.TagLabels;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.living.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Sarcio builds the name tag's text without calling getDisplayName, so the text is labelled here, where it is
// stored. The sneaking tag is drawn from this variable too, without going through EntityRendererMixin's method.
@Mixin(value = LivingEntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract class LivingEntityRendererMixin {
    @ModifyVariable(
        method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V",
        at = @At("STORE"),
        ordinal = 0,
        require = 0
    )
    private String urchin$labelNameTag(String text, @Local(argsOnly = true) LivingEntity entity) {
        return TagLabels.nametagText(entity, text);
    }
}
*///?}
