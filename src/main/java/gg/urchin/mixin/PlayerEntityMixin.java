//? if = 1.8.9 {
/*package gg.urchin.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gg.urchin.client.tags.TagLabels;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Text urchin$labelTaggedPlayer(Text name) {
        return TagLabels.nametag((PlayerEntity) (Object) this, name);
    }
}
*///?}
