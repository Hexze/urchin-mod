//? if = 1.8.9 {
/*package gg.urchin.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import gg.urchin.client.tags.TagLabels;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

// Wraps the whole method, outside other mods' display name caches and early returns.
@Mixin(value = PlayerEntity.class, priority = Integer.MAX_VALUE)
public abstract class PlayerEntityMixin {
    @WrapMethod(method = "getDisplayName")
    private Text urchin$labelTaggedPlayer(Operation<Text> original) {
        return TagLabels.nametag((PlayerEntity) (Object) this, original.call());
    }
}
*///?}
