//? if = 1.8.9 {
/*package gg.urchin.client.render;

import gg.urchin.UrchinConstants;
import gg.urchin.api.TagIcon;
import gg.urchin.client.chat.UrchinLogo;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.texture.DynamicTexture;
import net.minecraft.resource.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

// Draws Urchin's private-use glyphs (the logo and the MDI tag icons) as textured quads while the 1.8.9
// text renderer walks a string, so they show up anywhere text is drawn.
public final class GlyphQuads {
    // Every glyph is a square this many GUI pixels tall, raised so it lines up with the text.
    public static final float SIZE = 9f;
    public static final int ADVANCE = UrchinLogo.ADVANCE;
    private static final float RAISE = 1f;
    private static final char FIRST = UrchinLogo.GLYPH;
    private static final char LAST = TagIcon.GAP;

    private static final Logger LOG = LogManager.getLogger("Urchin/Glyphs");
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();
    private static final Set<Integer> SMOOTHED = new HashSet<>();
    private static final FloatBuffer COLOR = BufferUtils.createFloatBuffer(16);

    private GlyphQuads() {
    }

    // Whether the text has a glyph that must go through the vanilla per-glyph renderer.
    public static boolean containsGlyph(String text) {
        for (int i = 0, n = text.length(); i < n; i++) {
            char c = text.charAt(i);
            if (c >= FIRST && c <= LAST) return true;
        }
        return false;
    }

    public static boolean isGlyph(char c) {
        return c == FIRST || c == TagIcon.GAP || TagIcon.byGlyph(c) != null;
    }

    // How far the text cursor moves past a glyph.
    public static int advance(char c) {
        if (c == TagIcon.GAP) return TagIcon.GAP_ADVANCE;
        return c == FIRST ? ADVANCE : TagIcon.ICON_ADVANCE;
    }

    // Draws the full-colour logo, keeping only the text's alpha.
    public static void drawLogo(float x, float y, float alpha) {
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        Minecraft.getInstance().getTextureManager().bind(texture("textures/font/logo.png"));
        GL11.glGetFloatv(GL11.GL_CURRENT_COLOR, COLOR);
        GL11.glColor4f(1f, 1f, 1f, alpha);
        quad(x, y);
        GL11.glColor4f(COLOR.get(0), COLOR.get(1), COLOR.get(2), COLOR.get(3));
        GlStateManager.bindTexture(previousTexture);
    }

    // Draws a white icon tinted by the current text colour, so formatting codes colour it.
    public static void drawIcon(TagIcon icon, float x, float y) {
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        Minecraft.getInstance().getTextureManager().bind(texture("textures/tags/" + icon.getTexture() + ".png"));
        smooth();
        quad(x, y);
        GlStateManager.bindTexture(previousTexture);
    }

    // Read from the mod jar rather than the resource manager: plain 1.8.9 (without OSL's resource
    // loader, which OneConfig brings) does not serve mod assets, so urchin:textures/... would be missing.
    private static Identifier texture(String path) {
        Identifier id = TEXTURES.get(path);
        if (id != null) return id;
        BufferedImage image;
        try (InputStream in = GlyphQuads.class.getResourceAsStream("/assets/" + UrchinConstants.ID + "/" + path)) {
            if (in == null) throw new IOException("not in the mod jar");
            image = ImageIO.read(in);
        } catch (IOException | IllegalArgumentException e) {
            LOG.warn("Couldn't load {}", path, e);
            image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
        String name = UrchinConstants.ID + "_" + path.substring(path.lastIndexOf('/') + 1, path.length() - ".png".length());
        id = Minecraft.getInstance().getTextureManager().register(name, new DynamicTexture(image));
        TEXTURES.put(path, id);
        return id;
    }

    // The icons are 64px; mipmaps keep them clean at 9px instead of nearest-neighbour noise.
    private static void smooth() {
        int id = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        if (!SMOOTHED.add(id)) return;
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        try {
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        } catch (RuntimeException | LinkageError e) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        }
    }

    private static void quad(float x, float y) {
        float top = y - RAISE;
        float bottom = top + SIZE;
        float right = x + SIZE;
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
    }
}
*///?}
