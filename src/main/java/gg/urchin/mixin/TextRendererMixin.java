//? if = 1.8.9 {
/*package gg.urchin.mixin;

import gg.urchin.UrchinConstants;
import gg.urchin.client.chat.UrchinLogo;
import java.nio.FloatBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.resource.Identifier;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {
    @Unique
    private static final Identifier URCHIN_LOGO = new Identifier(UrchinConstants.ID, "textures/font/logo.png");
    @Unique
    private static final float LOGO_SIZE = 9f;
    @Unique
    private static final float LOGO_RAISE = 1f;
    @Unique
    private static final FloatBuffer URCHIN_COLOR = BufferUtils.createFloatBuffer(16);

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
    private void urchin$drawLogo(char glyph, boolean italic, CallbackInfoReturnable<Float> cir) {
        if (glyph != UrchinLogo.GLYPH) return;
        if (!urchin$drawingShadow) urchin$drawLogoQuad();
        cir.setReturnValue((float) UrchinLogo.ADVANCE);
    }

    @Inject(method = "getWidth(C)I", at = @At("HEAD"), cancellable = true)
    private void urchin$logoWidth(char glyph, CallbackInfoReturnable<Integer> cir) {
        if (glyph == UrchinLogo.GLYPH) cir.setReturnValue(UrchinLogo.ADVANCE);
    }

    @Unique
    private void urchin$drawLogoQuad() {
        float top = y - LOGO_RAISE;
        float bottom = top + LOGO_SIZE;
        float right = x + LOGO_SIZE;
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        Minecraft.getInstance().getTextureManager().bind(URCHIN_LOGO);
        GL11.glGetFloatv(GL11.GL_CURRENT_COLOR, URCHIN_COLOR);
        GL11.glColor4f(1f, 1f, 1f, a);
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(x, top, 0);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(x, bottom, 0);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(right, top, 0);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(right, bottom, 0);
        GL11.glEnd();
        GL11.glColor4f(URCHIN_COLOR.get(0), URCHIN_COLOR.get(1), URCHIN_COLOR.get(2), URCHIN_COLOR.get(3));
        GlStateManager.bindTexture(previousTexture);
    }
}
*///?}
