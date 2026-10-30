//? if = 1.8.9 {
/*package gg.urchin.mixin;

import gg.urchin.client.chat.UrchinLogo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.rdh.argentum.impl.render.gui.TextBatcher", remap = false)
public abstract class ArgentumTextBatcherMixin {
    @Inject(method = "cacheable(Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private static void urchin$drawLogoThroughVanilla(String text, CallbackInfoReturnable<Boolean> cir) {
        if (text.indexOf(UrchinLogo.GLYPH) >= 0) cir.setReturnValue(false);
    }
}
*///?}
