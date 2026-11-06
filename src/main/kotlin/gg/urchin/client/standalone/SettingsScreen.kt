//? if = 1.8.9 {
/*package gg.urchin.client.standalone

import gg.urchin.client.settings.Settings
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiElement
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.Window
import net.minecraft.client.render.platform.GlStateManager
import net.minecraft.client.render.texture.DynamicTexture
import net.minecraft.resource.Identifier
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import java.awt.image.DataBufferInt
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.nio.IntBuffer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val TEXTURE_NAME = "urchin_settings"
private const val DIM = 0x65000000
private const val WINDOW_MARGIN = 24f
private const val MIN_SCALE = 0.3f
private const val GL_BGRA = 0x80E1
private const val GL_UNSIGNED_INT_8_8_8_8_REV = 0x8367

// The /urchin settings screen for standalone installs. Like the AUtils menu it is laid out in window
// pixels (1018 x 574 at the normal size, ignoring Minecraft's GUI scale) and only shrinks when the window is
// too small for it. SettingsMenu paints into an image and reports which part changed; this screen keeps that
// image as a texture, re-uploads just the changed part, and turns mouse input into menu actions.
class SettingsScreen : Screen() {
    private val menu = SettingsMenu(Settings.store)
    private var textureId: Identifier? = null
    private var textureWidth = 0
    private var textureHeight = 0
    private var upload: IntBuffer? = null
    private var guiScale = 2
    private var menuScale = 1f
    private var originX = 0
    private var originY = 0

    override fun init() {
        val mc = Minecraft.getInstance()
        guiScale = max(1, Window(mc).scale)
        val fit = min((mc.width - WINDOW_MARGIN) / SettingsMenu.WIDTH, (mc.height - WINDOW_MARGIN) / SettingsMenu.HEIGHT)
        menuScale = max(MIN_SCALE, min(1f, fit))
        originX = ((mc.width - SettingsMenu.WIDTH * menuScale) / 2f).roundToInt()
        originY = ((mc.height - SettingsMenu.HEIGHT * menuScale) / 2f).roundToInt()
        release()
        menu.invalidate()
    }

    override fun render(mouseX: Int, mouseY: Int, tickDelta: Float) {
        if (minecraft.world == null) renderBackground() else GuiElement.fill(0, 0, width, height, DIM)
        val x = toMenuX(mouseX)
        val y = toMenuY(mouseY)
        menu.draggingSlider?.let { menu.slideTo(it, x) }
        if (menu.draggingScrollbar) menu.dragScrollbar(y)
        menu.paint(x, y, menuScale)?.let(::sync)
        val id = textureId ?: return

        val pad = (SettingsMenu.PAD * menuScale).roundToInt()
        GlStateManager.pushMatrix()
        GlStateManager.scalef(1f / guiScale, 1f / guiScale, 1f)
        GlStateManager.disableDepthTest()
        GlStateManager.disableAlphaTest()
        GlStateManager.enableBlend()
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.color4f(1f, 1f, 1f, 1f)
        minecraft.textureManager.bind(id)
        GuiElement.drawTexture(
            originX - pad, originY - pad, 0f, 0f, textureWidth, textureHeight, textureWidth, textureHeight,
            textureWidth.toFloat(), textureHeight.toFloat(),
        )
        GlStateManager.disableBlend()
        GlStateManager.enableAlphaTest()
        GlStateManager.enableDepthTest()
        GlStateManager.popMatrix()
    }

    // Brings the texture up to date with the part of the image that was repainted.
    private fun sync(paint: SettingsMenu.Paint) {
        val image = paint.image
        var id = textureId
        if (id == null || image.width != textureWidth || image.height != textureHeight) {
            release()
            id = minecraft.textureManager.register(TEXTURE_NAME, DynamicTexture(image.width, image.height))
            textureId = id
            textureWidth = image.width
            textureHeight = image.height
            // The new texture is empty, so everything must be sent, not just this region.
            if (paint.w != image.width || paint.h != image.height) {
                menu.invalidate()
                return
            }
            minecraft.textureManager.bind(id)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        } else {
            minecraft.textureManager.bind(id)
        }
        val pixels = (image.raster.dataBuffer as DataBufferInt).data
        val needed = paint.w * paint.h
        val existing = upload
        val buffer = if (existing != null && existing.capacity() >= needed) existing else BufferUtils.createIntBuffer(needed)
        upload = buffer
        buffer.clear()
        for (row in 0 until paint.h) buffer.put(pixels, (paint.y + row) * image.width + paint.x, paint.w)
        buffer.flip()
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0)
        GL11.glTexSubImage2D(
            GL11.GL_TEXTURE_2D, 0, paint.x, paint.y, paint.w, paint.h, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer,
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (button != 0) return
        val x = toMenuX(mouseX)
        val y = toMenuY(mouseY)
        val hit = menu.hitAt(x, y) ?: return
        val store = Settings.store
        when (val action = hit.action) {
            SettingsMenu.Action.Close -> minecraft.openScreen(null)
            SettingsMenu.Action.Master -> {
                store.enabled = !store.enabled
                store.save()
            }
            is SettingsMenu.Action.Open -> menu.page = action.page
            is SettingsMenu.Action.Toggle -> {
                action.row.set(!action.row.get())
                store.save()
            }
            is SettingsMenu.Action.Slide -> {
                menu.draggingSlider = action.row
                menu.slideTo(action.row, x)
            }
            is SettingsMenu.Action.Press -> action.row.onPress()
            SettingsMenu.Action.Scrollbar -> {
                menu.draggingScrollbar = true
                menu.dragScrollbar(y)
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, button: Int) {
        if (menu.draggingSlider != null) Settings.store.save()
        menu.draggingSlider = null
        menu.draggingScrollbar = false
    }

    override fun handleMouse() {
        super.handleMouse()
        val wheel = eventWheel()
        if (wheel != 0) menu.wheel(wheel.toFloat())
    }

    override fun removed() {
        Settings.store.save()
        release()
    }

    override fun shouldPauseGame(): Boolean = false

    private fun release() {
        textureId?.let { minecraft.textureManager.close(it) }
        textureId = null
        textureWidth = 0
        textureHeight = 0
    }

    // Minecraft's mouse position is in GUI units; scale it back to window pixels (to the middle of the unit).
    private fun toMenuX(mouseX: Int) = (mouseX * guiScale + guiScale / 2f - originX) / menuScale

    private fun toMenuY(mouseY: Int) = (mouseY * guiScale + guiScale / 2f - originY) / menuScale

    companion object {
        @Volatile
        private var pending = false

        // Chat commands run while the chat screen is still open and it closes itself right after,
        // so the menu opens on the next client tick instead.
        fun openNextTick() {
            pending = true
        }

        @JvmStatic
        fun openIfPending() {
            if (!pending) return
            pending = false
            Minecraft.getInstance().openScreen(SettingsScreen())
        }

        // org.lwjgl.input.Mouse exists on LWJGL 2 and on LWJGL 3 compatibility layers (OneClient's Pylon),
        // but not at compile time here, so it is looked up once.
        private val wheelHandle: MethodHandle? = try {
            MethodHandles.publicLookup().findStatic(
                Class.forName("org.lwjgl.input.Mouse"), "getEventDWheel", MethodType.methodType(Int::class.javaPrimitiveType),
            )
        } catch (_: ReflectiveOperationException) {
            null
        }

        private fun eventWheel(): Int = try {
            wheelHandle?.invoke() as? Int ?: 0
        } catch (_: Throwable) {
            0
        }
    }
}
*///?}
