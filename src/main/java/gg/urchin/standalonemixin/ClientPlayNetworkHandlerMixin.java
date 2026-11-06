//? if = 1.8.9 {
/*package gg.urchin.standalonemixin;

import gg.urchin.client.standalone.HypixelBridge;
import gg.urchin.client.standalone.Standalone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerInfoS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Vanilla runs each handler once on the network thread (which only reschedules it) and again on the
// client thread, so these hooks act on the client-thread call only.
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "handlePlayerInfo", at = @At("HEAD"))
    private void urchin$playersListed(PlayerInfoS2CPacket packet, CallbackInfo ci) {
        if (Minecraft.getInstance().isOnSameThread()) Standalone.received(packet);
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void urchin$hypixelPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isOnSameThread()) return;
        if (HypixelBridge.receive(packet.getChannel(), packet.getData())) ci.cancel();
    }
}
*///?}
