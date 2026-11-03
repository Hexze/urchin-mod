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
//?}
