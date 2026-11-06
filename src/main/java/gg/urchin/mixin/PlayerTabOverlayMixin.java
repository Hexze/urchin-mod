package gg.urchin.mixin;

import gg.urchin.client.tags.TagLabels;
import org.spongepowered.asm.mixin.Mixin;
//? if = 1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.overlay.PlayerTabOverlay;
import net.minecraft.client.network.PlayerInfo;
*///?} else {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.At;
//?}

@Mixin(value = PlayerTabOverlay.class, priority = Integer.MAX_VALUE)
public abstract class PlayerTabOverlayMixin {
    //? if = 1.8.9 {
    /*// Wraps the whole method: AUtils (Hytils, Level Head) sets the return value at RETURN, which returns early and
    // would skip a return value modifier.
    @WrapMethod(method = "getDisplayName")
    private String urchin$labelTaggedPlayer(PlayerInfo player, Operation<String> original) {
        return TagLabels.tabName(player, original.call(player));
    }
    *///?} else {
    @ModifyReturnValue(method = "getNameForDisplay", at = @At("RETURN"))
    private Component urchin$labelTaggedPlayer(Component name, @Local(argsOnly = true) PlayerInfo player) {
        return TagLabels.tabName(player, name);
    }
    //?}
}
