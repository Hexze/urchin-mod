//? if = 1.8.9 {
/*package gg.urchin.standalonemixin;

import gg.urchin.client.standalone.Standalone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "setWorld(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V", at = @At("TAIL"))
    private void urchin$worldLoaded(ClientWorld world, String message, CallbackInfo ci) {
        if (world != null) Standalone.worldLoaded();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void urchin$tick(CallbackInfo ci) {
        Standalone.tick();
    }
}
*///?}
