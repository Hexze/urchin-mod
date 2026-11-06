//? if = 1.8.9 {
/*package gg.urchin.standalonemixin;

import gg.urchin.client.standalone.StandaloneCommands;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalClientPlayerEntity.class)
public abstract class LocalClientPlayerEntityMixin {
    // Covers typed commands and clicked chat buttons (/urchin confirm, /urchin untag, ...).
    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void urchin$clientCommands(String message, CallbackInfo ci) {
        if (StandaloneCommands.handle(message)) ci.cancel();
    }
}
*///?}
