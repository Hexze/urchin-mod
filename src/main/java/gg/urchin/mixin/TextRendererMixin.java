//? if = 1.8.9 {
/*package gg.urchin.mixin;

import gg.urchin.api.TagIcon;
import gg.urchin.client.chat.UrchinLogo;
import gg.urchin.client.render.GlyphQuads;
import net.minecraft.client.render.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {
    @Shadow
    private float x;
    @Shadow
    private float y;
    @Shadow
    private float a;

    @Unique
    private boolean urchin$drawingShadow;

    @Inject(method = "drawLayer(Ljava/lang/String;Z)V", at = @At("HEAD"))
    private void urchin$trackShadow(String text, boolean shadow, CallbackInfo ci) {
        urchin$drawingShadow = shadow;
    }

    @Inject(method = "drawGlyph", at = @At("HEAD"), cancellable = true)
    private void urchin$drawGlyph(char glyph, boolean italic, CallbackInfoReturnable<Float> cir) {
        if (glyph == UrchinLogo.GLYPH) {
            if (!urchin$drawingShadow) GlyphQuads.drawLogo(x, y, a);
            cir.setReturnValue((float) GlyphQuads.ADVANCE);
            return;
        }
        if (glyph == TagIcon.GAP) {
            cir.setReturnValue((float) TagIcon.GAP_ADVANCE);
            return;
        }
        TagIcon icon = TagIcon.byGlyph(glyph);
        if (icon == null) return;
        // The vanilla shadow of a non-ASCII glyph lands right under it, so only the main pass is drawn.
        if (!urchin$drawingShadow) GlyphQuads.drawIcon(icon, x, y);
        cir.setReturnValue((float) TagIcon.ICON_ADVANCE);
    }

    @Inject(method = "getWidth(C)I", at = @At("HEAD"), cancellable = true)
    private void urchin$glyphWidth(char glyph, CallbackInfoReturnable<Integer> cir) {
        if (GlyphQuads.isGlyph(glyph)) cir.setReturnValue(GlyphQuads.advance(glyph));
    }
}
*///?}
