//? if > 1.8.9 {
package gg.urchin.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import gg.urchin.client.tags.TagLabels;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @ModifyReturnValue(method = "getNameTag", at = @At("RETURN"))
    private Component urchin$labelTaggedPlayer(Component name, @Local(argsOnly = true) Entity entity) {
        return entity instanceof Player player ? TagLabels.nametag(player, name) : name;
    }
}
//?} else {
/*package gg.urchin.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import gg.urchin.client.tags.TagLabels;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

// Every standing name tag goes through this method, whichever mod built its text or draws it (AUtils' Nametags and
// Argentum's batching replace the body). Wrapping the whole method gets the label in before any of them see the text.
@Mixin(value = EntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract class EntityRendererMixin {
    @WrapMethod(method = "renderNameTag(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V")
    private void urchin$labelNameTag(Entity entity, String text, double x, double y, double z, int range, Operation<Void> original) {
        original.call(entity, TagLabels.nametagText(entity, text), x, y, z, range);
    }
}
*///?}
