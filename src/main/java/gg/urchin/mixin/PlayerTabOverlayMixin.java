package gg.urchin.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import gg.urchin.client.tags.TagLabels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if = 1.8.9 {
/*import net.minecraft.client.gui.overlay.PlayerTabOverlay;
import net.minecraft.client.network.PlayerInfo;
*///?} else {
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
//?}

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    //? if = 1.8.9 {
    /*@ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private String urchin$labelTaggedPlayer(String name, @Local(argsOnly = true) PlayerInfo player) {
        return TagLabels.tabName(player, name);
    }
    *///?} else {
    @ModifyReturnValue(method = "getNameForDisplay", at = @At("RETURN"))
    private Component urchin$labelTaggedPlayer(Component name, @Local(argsOnly = true) PlayerInfo player) {
        return TagLabels.tabName(player, name);
    }
    //?}
}
