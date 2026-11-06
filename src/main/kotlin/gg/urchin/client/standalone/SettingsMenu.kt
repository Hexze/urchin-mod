//? if = 1.8.9 {
/*package gg.urchin.client.standalone

import gg.urchin.UrchinConstants
import gg.urchin.api.TagType
import gg.urchin.client.settings.SettingsStore
import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.FontFormatException
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.Arc2D
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.EnumMap
import java.util.Locale
import javax.imageio.ImageIO
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.reflect.KMutableProperty0

// Laid out like the AUtils menu: real window pixels (a 1018 x 574 panel at the normal size, independent of
// Minecraft's GUI scale) with Roboto at Badlion's px sizes. The structure follows Seraph's settings page
// (category sidebar, setting cards with a title and a description, accent section headers) and the look follows
// the Lunar Client menu as recreated by LunarForge (dark translucent rounded panel, ON/OFF switches, blue
// slider knob, 125 ms hover fades). Painted with Java2D into one image that SettingsScreen keeps as a texture.
//
// Painting is incremental: only the regions that changed since the last frame (a hovered card, an animating
// switch, the scrolled content) are cleared and repainted, and only those are re-uploaded.
internal class SettingsMenu(private val settings: SettingsStore) {
    enum class Page(val title: String, val icon: String) {
        ALERTS("Alerts", "bell"),
        LABELS("Labels", "tag"),
        FILTERS("Filters", "filter"),
        ACCOUNT("Account", "account"),
    }

    sealed interface Action {
        data object Close : Action
        data object Master : Action
        data class Open(val page: Page) : Action
        data class Toggle(val row: ToggleRow) : Action
        data class Slide(val row: SliderRow) : Action
        data class Press(val row: ButtonRow) : Action
        data object Scrollbar : Action
    }

    class Hit(val x: Float, val y: Float, val w: Float, val h: Float, val action: Action) {
        fun contains(px: Float, py: Float) = px >= x && px < x + w && py >= y && py < y + h
    }

    sealed class Row(val title: String, val description: String)
    class Section(title: String) : Row(title, "")
    class ToggleRow(title: String, description: String, val get: () -> Boolean, val set: (Boolean) -> Unit) : Row(title, description)
    class SliderRow(
        title: String,
        description: String,
        val min: Float,
        val max: Float,
        val step: Float,
        val unit: String,
        val get: () -> Float,
        val set: (Float) -> Unit,
    ) : Row(title, description)
    class ButtonRow(title: String, description: String, val label: String, val onPress: () -> Unit) : Row(title, description)
    class InfoRow(title: String, description: String) : Row(title, description)
    class PreviewRow(title: String, description: String) : Row(title, description)

    // The changed part of the image: pixel bounds inside `image`. Upload exactly this.
    class Paint(val image: BufferedImage, val x: Int, val y: Int, val w: Int, val h: Int)

    private class Zone(val x: Float, val y: Float, val w: Float, val h: Float, val over: Boolean) {
        fun contains(px: Float, py: Float) = px >= x && px < x + w && py >= y && py < y + h
    }

    private class Placed(val row: Row, val y: Float, val h: Float, val lines: List<String>)

    private class Layout(val rows: List<Placed>, val total: Float)

    private class Dirty {
        var x0 = Float.MAX_VALUE
        var y0 = Float.MAX_VALUE
        var x1 = -Float.MAX_VALUE
        var y1 = -Float.MAX_VALUE
        val any get() = x1 >= x0

        fun add(x: Float, y: Float, w: Float, h: Float) {
            x0 = min(x0, x)
            y0 = min(y0, y)
            x1 = max(x1, x + w)
            y1 = max(y1, y + h)
        }

        fun clear() {
            x0 = Float.MAX_VALUE
            y0 = Float.MAX_VALUE
            x1 = -Float.MAX_VALUE
            y1 = -Float.MAX_VALUE
        }
    }

    private data class TextKey(val font: Font, val size: Float, val text: String)

    var page = Page.ALERTS
        set(value) {
            field = value
            smooth.jumpTo(0f)
            invalidate()
        }
    val hits = mutableListOf<Hit>()
    var draggingSlider: SliderRow? = null
    var draggingScrollbar = false

    private val smooth = SmoothScroll(SCROLL_PER_NOTCH)
    private var maxScroll = 0f
    private var image: BufferedImage? = null
    private var imageScale = 0f
    private var full = true
    private var lastSignature = 0
    private val dirty = Dirty()
    private var carry = ArrayList<FloatArray>()
    private var nextCarry = ArrayList<FloatArray>()
    private var zones = ArrayList<Zone>()
    private var newZones = ArrayList<Zone>()
    private var region = FloatArray(4)
    private val rowRects = HashMap<Row, FloatArray>()
    private var g: Graphics2D? = null
    private var mx = -1f
    private var my = -1f
    private val fades = HashMap<String, LongArray>()
    private val slides = HashMap<String, LongArray>()
    private val images = HashMap<String, BufferedImage>()
    private val frc = FontRenderContext(AffineTransform(), true, true)
    private val widths = HashMap<TextKey, Float>()
    private val ascents = HashMap<Pair<Font, Float>, Float>()
    private val layouts = EnumMap<Page, Layout>(Page::class.java)

    private val pages: Map<Page, List<Row>> = mapOf(
        Page.ALERTS to listOf(
            Section("Alerts"),
            toggle("Enabled", "Send a chat alert when a tagged player joins your game.", settings::alerts),
            toggle("Sound", "Play a ping with each alert.", settings::alertSound),
            SliderRow(
                "Delay", "Wait this long after a player joins before alerting.", 0f, 1000f, 250f, "ms",
                { settings.alertDelay }, { settings.alertDelay = it },
            ),
            toggle("Compact", "Show each tagged player as one line of badges. Hover a badge for the details.", settings::compactAlerts),
        ),
        Page.LABELS to listOf(
            Section("Labels"),
            toggle("Tab List", "Show each tagged player's tag next to their name in the tab list.", settings::tabLabels),
            toggle("Nametags", "Show each tagged player's tag next to the name above their head.", settings::nametagLabels),
            toggle("Chat", "Show tags next to tagged players' names in chat. Hover a tag for the details.", settings::chatLabels),
            Section("Style"),
            toggle(
                "MDI Icons",
                "Show tags as Material Design icons instead of letters like [CCC], in chat, the tab list and nametags.",
                settings::tagIcons,
            ),
            PreviewRow("Preview", "How each tag type looks with the current style."),
        ),
        Page.FILTERS to listOf(
            Section("Filters"),
            toggle("Ignore Yourself", "Skip alerts for your own tags. Your labels still show.", settings::ignoreSelf),
            toggle("Ignore Your Party", "Skip alerts for players in your Hypixel party. Their labels still show.", settings::ignoreParty),
        ),
        Page.ACCOUNT to listOf(
            Section("Account"),
            InfoRow(
                "Limited Access",
                "You can request and view blacklist tags. To use other features, provide an API key or link this " +
                    "Minecraft account to your Discord account in the Urchin Discord.",
            ),
            ButtonRow("Urchin Discord", "discord.gg/urchin", "JOIN") { Links.open(DISCORD_INVITE) },
            Section("Commands"),
            InfoRow(
                "Chat commands",
                "/urchin opens this screen. /urchin <players>, /urchin tag <player> <type> " +
                    "<reason> and /urchin untag <player> <type> look up, add and remove tags.",
            ),
        ),
    )

    private fun toggle(title: String, description: String, property: KMutableProperty0<Boolean>) =
        ToggleRow(title, description, property::get, property::set)

    // ---- input ----

    // Repaint everything on the next frame.
    fun invalidate() {
        full = true
    }

    // A mouse wheel turn (positive is up, 120 per notch).
    fun wheel(raw: Float) = smooth.wheel(raw)

    fun dragScrollbar(y: Float) {
        val view = CONTENT_BOTTOM - CONTENT_TOP
        val thumb = thumbHeight(view)
        val fraction = ((y - CONTENT_TOP - thumb / 2) / (view - thumb)).coerceIn(0f, 1f)
        smooth.dragTo(fraction * maxScroll)
    }

    fun hitAt(x: Float, y: Float): Hit? = hits.lastOrNull { it.contains(x, y) }

    fun slideTo(row: SliderRow, x: Float) {
        val left = sliderLeft()
        val fraction = ((x - left) / SLIDER_WIDTH).coerceIn(0f, 1f)
        val raw = row.min + fraction * (row.max - row.min)
        val value = ((raw / row.step).roundToInt() * row.step).coerceIn(row.min, row.max)
        if (value == row.get()) return
        row.set(value)
        lastSignature = signature()
        rowRects[row]?.let { dirty.add(it[0], it[1], it[2], it[3]) } ?: invalidate()
    }

    private fun signature(): Int =
        listOf(
            settings.enabled, settings.alerts, settings.alertSound, settings.alertDelay, settings.compactAlerts,
            settings.tabLabels, settings.nametagLabels, settings.chatLabels, settings.tagIcons, settings.ignoreSelf,
            settings.ignoreParty,
        ).hashCode()

    // ---- painting ----

    // Brings the image up to date for the mouse position and returns the part that changed, or null when
    // nothing did. `scale` is image pixels per menu pixel (1 at the normal size).
    fun paint(mouseX: Float, mouseY: Float, scale: Float): Paint? {
        val target = ensureImage(scale)
        mx = mouseX
        my = mouseY

        val current = layout(page)
        maxScroll = max(0f, current.total - (CONTENT_BOTTOM - CONTENT_TOP))
        val sig = signature()
        if (sig != lastSignature) {
            lastSignature = sig
            full = true
        }
        carry.forEach { dirty.add(it[0], it[1], it[2], it[3]) }
        carry.clear()
        for (z in zones) if (z.contains(mx, my) != z.over) dirty.add(z.x, z.y, z.w, z.h)
        if (smooth.update(maxScroll)) dirty.add(CONTENT_LEFT, CONTENT_TOP, CONTENT_RIGHT - CONTENT_LEFT, CONTENT_BOTTOM - CONTENT_TOP)
        if (!full && !dirty.any) return null

        val bounds = if (full) Rectangle(0, 0, target.width, target.height) else pixelBounds(scale, target)
        draw(target, bounds, scale)
        full = false
        dirty.clear()
        return Paint(target, bounds.x, bounds.y, bounds.width, bounds.height)
    }

    private fun ensureImage(scale: Float): BufferedImage {
        val existing = image
        if (existing != null && imageScale == scale) return existing
        val created = BufferedImage(
            ceil((WIDTH + PAD * 2) * scale).toInt(), ceil((HEIGHT + PAD * 2) * scale).toInt(), BufferedImage.TYPE_INT_ARGB,
        )
        image = created
        imageScale = scale
        full = true
        return created
    }

    private fun pixelBounds(scale: Float, target: BufferedImage): Rectangle {
        val x0 = max(0, floor((dirty.x0 + PAD) * scale).toInt() - MARGIN_PX)
        val y0 = max(0, floor((dirty.y0 + PAD) * scale).toInt() - MARGIN_PX)
        val x1 = min(target.width, ceil((dirty.x1 + PAD) * scale).toInt() + MARGIN_PX)
        val y1 = min(target.height, ceil((dirty.y1 + PAD) * scale).toInt() + MARGIN_PX)
        return Rectangle(x0, y0, max(1, x1 - x0), max(1, y1 - y0))
    }

    private fun draw(target: BufferedImage, bounds: Rectangle, scale: Float) {
        val graphics = target.createGraphics()
        g = graphics
        graphics.setClip(bounds.x, bounds.y, bounds.width, bounds.height)
        graphics.composite = AlphaComposite.Clear
        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
        graphics.composite = AlphaComposite.SrcOver
        graphics.scale(scale.toDouble(), scale.toDouble())
        graphics.translate(PAD.toDouble(), PAD.toDouble())
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        hits.clear()
        newZones.clear()
        nextCarry.clear()
        try {
            panel()
            header()
            sidebar()
            content()
        } finally {
            graphics.dispose()
            g = null
        }
        val swapZones = zones
        zones = newZones
        newZones = swapZones
        val swapCarry = carry
        carry = nextCarry
        nextCarry = swapCarry
    }

    private fun panel() {
        ring(-1f, -1f, WIDTH + 2, HEIGHT + 2, 0x601E1E1E, RADIUS + 1f)
        ring(0f, 0f, WIDTH, HEIGHT, 0x401E1E1E)
        fill(0f, 0f, WIDTH, HEIGHT, 0xB21A1818.toInt())
    }

    private fun header() {
        fill(0f, 0f, WIDTH, HEADER, 0x36000000, tl = true, tr = true, bl = false, br = false)
        rect(0f, HEADER, WIDTH, 1f, 0x20FFFFFF)
        image("icon.png", 18f, 14f, 32f, 32f, 1f)
        val title = spaced("URCHIN")
        text(title, Fonts.bold, F_HEAD, 62f, 17f, WHITE)
        text("Blacklist tags", Fonts.light, F_DESC, 62f + width(title, Fonts.bold, F_HEAD) + 14f, 24f, 0x70FFFFFF)

        val closeX = WIDTH - 18f - 32f
        region(closeX, 14f, 32f, 32f)
        val close = hover(closeX, 14f, 32f, 32f)
        body(closeX, 14f, 32f, 32f, 0x40252525, 0x20FFFFFF, fadeColor("close", close, 0x20FFFFFF, 0x50FFFFFF))
        icon("close", closeX + 8f, 22f, 16f, fadeColor("closeIcon", close, 0x50FFFFFF, 0xAFFFFFFF.toInt()))
        hit(closeX, 14f, 32f, 32f, Action.Close)

        val on = settings.enabled
        val masterX = closeX - 12f - MASTER_W
        region(masterX, 14f, MASTER_W, 32f)
        val over = hover(masterX, 14f, MASTER_W, 32f)
        val color = if (on) {
            fadeColor("master", over, 0xAF29D67A.toInt(), 0xFF29D67A.toInt())
        } else {
            fadeColor("master", over, 0xAFDE2152.toInt(), 0xFFDE2152.toInt())
        }
        fill(masterX, 14f, MASTER_W, 32f, color)
        ring(masterX + 1f, 15f, MASTER_W - 2f, 30f, 0x35FFFFFF, RADIUS - 1f)
        icon("power", masterX + 16f, 22f, 16f, WHITE)
        centered(spaced(if (on) "ENABLED" else "DISABLED"), Fonts.bold, F_BUTTON, masterX + MASTER_W / 2 + 10f, 23f, WHITE)
        hit(masterX, 14f, MASTER_W, 32f, Action.Master)
    }

    private fun sidebar() {
        fill(0f, HEADER + 1f, SIDEBAR, HEIGHT - HEADER - 1f, 0x20000000, tl = false, tr = false, bl = true, br = false)
        rect(SIDEBAR, HEADER + 1f, 1f, HEIGHT - HEADER - 1f, 0x20FFFFFF)
        text(spaced("SETTINGS"), Fonts.bold, F_SMALL, 24f, HEADER + 22f, 0x60FFFFFF)
        var y = HEADER + 46f
        for (entry in Page.entries) {
            val x = 12f
            val w = SIDEBAR - 24f
            region(x, y, w, NAV_HEIGHT)
            val active = entry == page
            val over = hover(x, y, w, NAV_HEIGHT)
            val bg = if (active) 0x334F94FC else fadeColor("nav:$entry", over, 0x10FFFFFF, 0x22FFFFFF)
            fill(x, y, w, NAV_HEIGHT, bg)
            if (active) fill(x, y + 8f, 3f, NAV_HEIGHT - 16f, ACCENT)
            val tint = if (active) ACCENT else fadeColor("navIcon:$entry", over, 0x99FFFFFF.toInt(), 0xDDFFFFFF.toInt())
            icon(entry.icon, x + 14f, y + (NAV_HEIGHT - 20f) / 2, 20f, tint)
            text(entry.title, Fonts.medium, F_LABEL, x + 46f, y + (NAV_HEIGHT - 21f) / 2, if (active) WHITE else LABEL)
            hit(x, y, w, NAV_HEIGHT, Action.Open(entry))
            y += NAV_HEIGHT + NAV_GAP
        }
    }

    private fun content() {
        val graphics = g ?: return
        val laid = layout(page)
        val width = CONTENT_RIGHT - CONTENT_LEFT - SCROLLBAR_ROOM
        val oldClip = graphics.clip
        graphics.clip(Rectangle2D.Float(CONTENT_LEFT - 4f, CONTENT_TOP - 4f, CONTENT_RIGHT - CONTENT_LEFT + 8f, CONTENT_BOTTOM - CONTENT_TOP + 8f))
        val disabled = !settings.enabled
        val oldComposite = graphics.composite
        if (disabled) graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, DISABLED_ALPHA)
        val offset = smooth.offset
        for (placed in laid.rows) {
            val y = CONTENT_TOP + placed.y - offset
            if (y + placed.h >= CONTENT_TOP - 4f && y <= CONTENT_BOTTOM + 4f) drawRow(placed, CONTENT_LEFT, y, width, !disabled)
        }
        graphics.composite = oldComposite
        graphics.clip = oldClip
        scrollbar(offset)
    }

    private fun layout(page: Page): Layout = layouts.getOrPut(page) {
        val width = CONTENT_RIGHT - CONTENT_LEFT - SCROLLBAR_ROOM
        var y = 0f
        val placed = pages.getValue(page).map { row ->
            val lines = if (row is Section || row is PreviewRow && false) emptyList() else descriptionLines(row, width)
            val h = when (row) {
                is Section -> SECTION_HEIGHT
                is PreviewRow -> CARD_PAD + 21f + 4f + lines.size * LINE + 12f + PREVIEW_ICON + CARD_PAD
                else -> max(CARD_MIN, CARD_PAD + 21f + 4f + lines.size * LINE + CARD_PAD)
            }
            Placed(row, y, h, lines).also { y += h + ROW_GAP }
        }
        Layout(placed, y - ROW_GAP + CONTENT_END_PAD)
    }

    private fun descriptionLines(row: Row, width: Float): List<String> =
        wrap(row.description, Fonts.light, F_DESC, width - CARD_PAD * 2 - controlWidth(row) - 24f)

    private fun controlWidth(row: Row): Float = when (row) {
        is ToggleRow -> SWITCH_W
        is SliderRow -> SLIDER_WIDTH + 70f
        is ButtonRow -> BUTTON_W
        is InfoRow -> 24f
        else -> 0f
    }

    private fun region(x: Float, y: Float, w: Float, h: Float) {
        region = floatArrayOf(x, y, w, h)
    }

    private fun drawRow(placed: Placed, x: Float, y: Float, w: Float, interactive: Boolean) {
        val row = placed.row
        val h = placed.h
        region(x, y, w, h)
        if (row is Section) {
            val label = spaced(row.title.uppercase(Locale.ROOT))
            text(label, Fonts.bold, F_SECTION, x + 4f, y + 8f, ACCENT)
            val end = x + 4f + width(label, Fonts.bold, F_SECTION) + 12f
            rect(end, y + 16f, x + w - end, 1f, 0x30FFFFFF)
            return
        }
        rowRects[row] = floatArrayOf(x, y, w, h)
        val over = interactive && hover(x, y, w, h) && my >= CONTENT_TOP && my < CONTENT_BOTTOM
        fill(x, y, w, h, fadeColor("card:${row.title}", over, 0x20575757, 0x34676767))
        ring(x, y, w, h, 0x20E2E2E2)
        text(row.title, Fonts.medium, F_LABEL, x + CARD_PAD, y + CARD_PAD, 0xFFE6E6E6.toInt())
        var lineY = y + CARD_PAD + 21f + 4f
        for (line in placed.lines) {
            text(line, Fonts.light, F_DESC, x + CARD_PAD, lineY, 0x8CFFFFFF.toInt())
            lineY += LINE
        }
        val right = x + w - CARD_PAD
        when (row) {
            is ToggleRow -> {
                switch(right - SWITCH_W, y + h / 2 - SWITCH_H / 2, row.get(), row.title, over)
                if (interactive) hitClip(x, y, w, h, Action.Toggle(row))
            }
            is SliderRow -> slider(row, right, y + h / 2, interactive)
            is ButtonRow -> {
                val bx = right - BUTTON_W
                val by = y + h / 2 - 16f
                val lit = interactive && hover(bx, by, BUTTON_W, 32f)
                val fillColor = fadeColor("button:${row.title}", lit, 0x805788D2.toInt(), 0xFF4F94FC.toInt())
                body(bx, by, BUTTON_W, 32f, 0, if (lit) 0x60A2A2A2 else 0x20A2A2A2, fillColor)
                icon("open", bx + 16f, by + 8f, 16f, if (lit) WHITE else 0xCCFFFFFF.toInt())
                centered(spaced(row.label), Fonts.bold, F_BUTTON, bx + BUTTON_W / 2 + 10f, by + 9f, if (lit) WHITE else 0xDDFFFFFF.toInt())
                if (interactive) hitClip(bx, by, BUTTON_W, 32f, Action.Press(row))
            }
            is InfoRow -> icon("info", right - 22f, y + CARD_PAD, 22f, 0x60FFFFFF)
            is PreviewRow -> preview(x + CARD_PAD, y + h - CARD_PAD - PREVIEW_ICON)
            is Section -> Unit
        }
    }

    // Lunar's labelled ON/OFF switch.
    private fun switch(x: Float, y: Float, on: Boolean, key: String, hovered: Boolean) {
        fill(x, y, SWITCH_W, SWITCH_H, if (hovered) 0x40E2E2E2 else 0x20E2E2E2)
        ring(x, y, SWITCH_W, SWITCH_H, 0x35FFFFFF)
        val thumb = x + (SWITCH_W - THUMB_W) * slide("switch:$key", on)
        fill(thumb, y, THUMB_W, SWITCH_H, if (on) 0xAF29D67A.toInt() else 0xAFDE2152.toInt())
        ring(thumb, y, THUMB_W, SWITCH_H, 0x35FFFFFF)
        centered(if (on) "ON" else "OFF", Fonts.bold, F_SMALL, thumb + THUMB_W / 2, y + 5f, WHITE)
    }

    private fun sliderLeft(): Float = CONTENT_RIGHT - SCROLLBAR_ROOM - CARD_PAD - SLIDER_WIDTH

    private fun slider(row: SliderRow, right: Float, cy: Float, interactive: Boolean) {
        val graphics = g ?: return
        val left = right - SLIDER_WIDTH
        val value = row.get().coerceIn(row.min, row.max)
        val fraction = (value - row.min) / (row.max - row.min)
        val shown = "${value.roundToInt()} ${row.unit}"
        text(shown, Fonts.bold, F_BUTTON, left - 16f - width(shown, Fonts.bold, F_BUTTON), cy - 8f, 0x8FFFFFFF.toInt())
        rect(left, cy - 2.5f, SLIDER_WIDTH, 5f, 0xFF101111.toInt())
        rect(left, cy - 2.5f, SLIDER_WIDTH * fraction, 5f, 0x804F94FC.toInt())
        val steps = ((row.max - row.min) / row.step).roundToInt()
        for (i in 0..steps) rect(left + SLIDER_WIDTH * i / steps - 0.5f, cy + 8f, 1f, 5f, 0x40FFFFFF)
        val held = draggingSlider === row
        val knobOver = interactive && hover(left - 10f, cy - 14f, SLIDER_WIDTH + 20f, 28f)
        val r = 9f + 1.5f * fade("knob:${row.title}", held || knobOver)
        val kx = left + SLIDER_WIDTH * fraction
        fill(kx - r, cy - r, r * 2, r * 2, ACCENT)
        ring(kx - r, cy - r, r * 2, r * 2, 0x40FFFFFF)
        if (interactive) hitClip(left - 12f, cy - 14f, SLIDER_WIDTH + 24f, 28f, Action.Slide(row))
    }

    private fun preview(x: Float, y: Float) {
        val order = listOf(
            TagType.CONFIRMED_CHEATER, TagType.CLOSET_CHEATER, TagType.BLATANT_CHEATER,
            TagType.CAUTION, TagType.SNIPER, TagType.REPLAYS_NEEDED,
        )
        var cx = x
        for (type in order) {
            val icon = type.mdi
            if (settings.tagIcons && icon != null) {
                tintedImage("tags/${icon.texture}.png", cx, y, PREVIEW_ICON, PREVIEW_ICON, 0xFF000000.toInt() or type.mdiColor.value())
                cx += PREVIEW_ICON + 6f
            } else {
                val top = y + 3f
                text("[", Fonts.bold, F_LABEL, cx, top, DARK_GRAY)
                cx += width("[", Fonts.bold, F_LABEL)
                text(type.icon, Fonts.bold, F_LABEL, cx, top, 0xFF000000.toInt() or type.color.value())
                cx += width(type.icon, Fonts.bold, F_LABEL)
                text("]", Fonts.bold, F_LABEL, cx, top, DARK_GRAY)
                cx += width("]", Fonts.bold, F_LABEL) + 6f
            }
            val alias = type.alias.uppercase(Locale.ROOT)
            text(alias, Fonts.light, F_SMALL, cx, y + 7f, 0x60FFFFFF)
            cx += width(alias, Fonts.light, F_SMALL) + 26f
        }
    }

    private fun thumbHeight(view: Float) = max(32f, view * view / (view + maxScroll))

    private fun scrollbar(offset: Float) {
        if (maxScroll <= 0f) return
        val x = CONTENT_RIGHT - 6f
        val view = CONTENT_BOTTOM - CONTENT_TOP
        val thumb = thumbHeight(view)
        val ty = CONTENT_TOP + (view - thumb) * (offset / maxScroll)
        region(x - 4f, CONTENT_TOP, 14f, view)
        val over = hover(x - 2f, ty, 10f, thumb)
        rect(x, CONTENT_TOP, 6f, view, 0x203D3D3D)
        fill(x, ty, 6f, thumb, if (over || draggingScrollbar) 0x60E2E2E2 else 0x20E2E2E2)
        hit(x - 4f, CONTENT_TOP, 14f, view, Action.Scrollbar)
    }

    // ---- primitives ----

    private fun hit(x: Float, y: Float, w: Float, h: Float, action: Action) {
        hits += Hit(x, y, w, h, action)
    }

    private fun hitClip(x: Float, y: Float, w: Float, h: Float, action: Action) {
        val top = max(CONTENT_TOP, y)
        val bottom = min(CONTENT_BOTTOM, y + h)
        if (bottom > top) hit(x, top, w, bottom - top, action)
    }

    private fun hover(x: Float, y: Float, w: Float, h: Float): Boolean {
        val over = mx >= x && mx < x + w && my >= y && my < y + h
        newZones += Zone(x, y, w, h, over)
        return over
    }

    // Keeps the current element's region dirty for another frame while one of its animations is running.
    private fun animateRegion() {
        nextCarry += region
    }

    private fun fade(key: String, on: Boolean): Float {
        val now = System.currentTimeMillis()
        val state = fades.getOrPut(key) { longArrayOf(if (on) 1 else 0, 0) }
        if ((state[0] == 1L) != on) {
            state[0] = if (on) 1 else 0
            state[1] = now
        }
        val progress = min(1f, (now - state[1]) / FADE_MS)
        if (progress < 1f) animateRegion()
        return if (on) progress else 1 - progress
    }

    private fun slide(key: String, on: Boolean): Float {
        val now = System.currentTimeMillis()
        val state = slides.getOrPut(key) { longArrayOf(if (on) 1 else 0, 0) }
        if ((state[0] == 1L) != on) {
            state[0] = if (on) 1 else 0
            state[1] = now
        }
        val linear = min(1f, (now - state[1]) / SLIDE_MS)
        if (linear < 1f) animateRegion()
        val t = linear * linear
        return if (on) t else 1 - t
    }

    private fun fadeColor(key: String, on: Boolean, from: Int, to: Int) = lerp(from, to, fade(key, on))

    private fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        val graphics = g ?: return
        graphics.color = Color(color, true)
        graphics.fill(Rectangle2D.Float(x, y, w, h))
    }

    private fun fill(
        x: Float, y: Float, w: Float, h: Float, color: Int,
        tl: Boolean = true, tr: Boolean = true, bl: Boolean = true, br: Boolean = true,
    ) {
        val graphics = g ?: return
        graphics.color = Color(color, true)
        graphics.fill(corners(x, y, w, h, min(RADIUS, min(w, h) / 2), tl, tr, bl, br))
    }

    // A 1 px outline drawn as a stroke along the inside edge of the box. `r` differs from RADIUS only for an outline
    // sitting just outside or inside a shape, so that its curve stays parallel to the shape's.
    private fun ring(x: Float, y: Float, w: Float, h: Float, color: Int, r: Float = RADIUS) {
        val graphics = g ?: return
        if ((color ushr 24) == 0) return
        graphics.color = Color(color, true)
        graphics.stroke = BasicStroke(1f)
        val arc = 2 * min(r, min(w, h) / 2)
        graphics.draw(RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1f, h - 1f, arc, arc))
    }

    private fun body(x: Float, y: Float, w: Float, h: Float, outer: Int, inner: Int, fill: Int) {
        fill(x, y, w, h, fill)
        ring(x, y, w, h, outer)
        ring(x + 1f, y + 1f, w - 2f, h - 2f, inner, RADIUS - 1f)
    }

    private fun image(path: String, x: Float, y: Float, w: Float, h: Float, alpha: Float) {
        val graphics = g ?: return
        val img = images.getOrPut(path) { load(path) }
        val old = graphics.composite
        graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * ((old as? AlphaComposite)?.alpha ?: 1f))
        val transform = AffineTransform()
        transform.translate(x.toDouble(), y.toDouble())
        transform.scale((w / img.width).toDouble(), (h / img.height).toDouble())
        graphics.drawImage(img, transform, null)
        graphics.composite = old
    }

    private fun icon(name: String, x: Float, y: Float, size: Float, argb: Int) = tintedImage("textures/gui/$name.png", x, y, size, size, argb)

    private fun tintedImage(path: String, x: Float, y: Float, w: Float, h: Float, argb: Int) {
        val key = path + "#" + Integer.toHexString(argb or -0x1000000)
        if (key !in images) {
            val source = images.getOrPut(path) { load(if (path.startsWith("tags/")) "textures/$path" else path) }
            val out = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB)
            for (py in 0 until source.height) for (px in 0 until source.width) {
                val c = source.getRGB(px, py)
                val r = ((c shr 16) and 0xFF) * ((argb shr 16) and 0xFF) / 255
                val gr = ((c shr 8) and 0xFF) * ((argb shr 8) and 0xFF) / 255
                val b = (c and 0xFF) * (argb and 0xFF) / 255
                out.setRGB(px, py, (c and -0x1000000) or (r shl 16) or (gr shl 8) or b)
            }
            images[key] = out
        }
        image(key, x, y, w, h, ((argb ushr 24) and 0xFF) / 255f)
    }

    private fun load(path: String): BufferedImage = try {
        SettingsMenu::class.java.getResourceAsStream("/assets/${UrchinConstants.ID}/$path")
            ?.use { ImageIO.read(it) } ?: throw IOException("missing $path")
    } catch (e: IOException) {
        throw IllegalStateException("Missing Urchin UI asset: $path", e)
    }

    // `y` is the top of the text's line box.
    private fun text(value: String, font: Font, size: Float, x: Float, y: Float, color: Int) {
        val graphics = g ?: return
        val derived = Fonts.sized(font, size)
        graphics.font = derived
        graphics.color = Color(if ((color ushr 24) == 0) color or -0x1000000 else color, true)
        graphics.drawString(value, x, y + ascent(font, size))
    }

    private fun centered(value: String, font: Font, size: Float, cx: Float, y: Float, color: Int) =
        text(value, font, size, cx - width(value, font, size) / 2, y, color)

    private fun ascent(font: Font, size: Float): Float =
        ascents.getOrPut(font to size) { Fonts.sized(font, size).getLineMetrics("Ag", frc).ascent }

    private fun width(value: String, font: Font, size: Float): Float =
        widths.getOrPut(TextKey(font, size, value)) { Fonts.sized(font, size).getStringBounds(value, frc).width.toFloat() }

    private fun wrap(value: String, font: Font, size: Float, maxWidth: Float): List<String> {
        if (value.isEmpty()) return emptyList()
        val lines = mutableListOf<String>()
        var line = StringBuilder()
        for (word in value.split(' ')) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (line.isNotEmpty() && width(candidate, font, size) > maxWidth) {
                lines += line.toString()
                line = StringBuilder(word)
            } else {
                line = StringBuilder(candidate)
            }
        }
        if (line.isNotEmpty()) lines += line.toString()
        return lines
    }

    private object Fonts {
        val bold: Font = load("Roboto-Bold.ttf")
        val medium: Font = load("Roboto-Medium.ttf")
        val light: Font = load("Roboto-Light.ttf")
        private val sizes = HashMap<Pair<Font, Float>, Font>()

        fun sized(font: Font, size: Float): Font = sizes.getOrPut(font to size) { font.deriveFont(size) }

        private fun load(file: String): Font = try {
            SettingsMenu::class.java.getResourceAsStream("/assets/${UrchinConstants.ID}/fonts/$file")
                ?.use { Font.createFont(Font.TRUETYPE_FONT, it) } ?: Font(Font.SANS_SERIF, Font.PLAIN, 1)
        } catch (e: FontFormatException) {
            Font(Font.SANS_SERIF, Font.PLAIN, 1)
        } catch (e: IOException) {
            Font(Font.SANS_SERIF, Font.PLAIN, 1)
        }
    }

    companion object {
        // The panel is 1018 x 574 pixels, the same as the AUtils menu at its normal size.
        const val WIDTH = 1018f
        const val HEIGHT = 574f
        const val PAD = 4f
        private const val MARGIN_PX = 2
        // The corner radius of every rounded shape in the menu. Shapes too small for it are as round as they can be.
        private const val RADIUS = 6f
        private const val HEADER = 60f
        private const val SIDEBAR = 200f
        private const val NAV_HEIGHT = 38f
        private const val NAV_GAP = 6f
        private const val CONTENT_LEFT = SIDEBAR + 24f
        private const val CONTENT_RIGHT = WIDTH - 24f
        private const val CONTENT_TOP = HEADER + 20f
        private const val CONTENT_BOTTOM = HEIGHT - 20f
        private const val CONTENT_END_PAD = 4f
        private const val SCROLLBAR_ROOM = 18f
        private const val SECTION_HEIGHT = 34f
        private const val CARD_MIN = 64f
        private const val CARD_PAD = 16f
        private const val LINE = 19f
        private const val ROW_GAP = 8f
        private const val SWITCH_W = 56f
        private const val SWITCH_H = 24f
        private const val THUMB_W = 34f
        private const val SLIDER_WIDTH = 200f
        private const val BUTTON_W = 124f
        private const val MASTER_W = 150f
        private const val PREVIEW_ICON = 28f
        private const val SCROLL_PER_NOTCH = 144f
        private const val FADE_MS = 125f
        private const val SLIDE_MS = 100f
        private const val DISABLED_ALPHA = 0.4f

        // Badlion's px font sizes (its theme uses 14 / 16 / 18 / 20 / 22).
        private const val F_DESC = 14f
        private const val F_LABEL = 18f
        private const val F_HEAD = 22f
        private const val F_SECTION = 13f
        private const val F_BUTTON = 13f
        private const val F_SMALL = 12f

        private const val WHITE = -0x1
        private const val LABEL = 0xFFC1C0BE.toInt()
        private const val ACCENT = 0xFF4F94FC.toInt()
        private const val DARK_GRAY = 0xFF555555.toInt()
        const val DISCORD_INVITE = "https://discord.gg/urchin"

        private fun spaced(text: String) = text.toCharArray().joinToString(" ")

        private fun lerp(a: Int, b: Int, t: Float): Int {
            var out = 0
            for (shift in 0 until 32 step 8) {
                val channel = (((a ushr shift) and 0xFF) * (1 - t) + ((b ushr shift) and 0xFF) * t).roundToInt()
                out = out or (channel shl shift)
            }
            return out
        }

        private fun corners(x: Float, y: Float, w: Float, h: Float, r: Float, tl: Boolean, tr: Boolean, bl: Boolean, br: Boolean): Shape {
            val path = Path2D.Float()
            path.moveTo(x + if (tl) r else 0f, y)
            path.lineTo(x + w - if (tr) r else 0f, y)
            if (tr) path.append(Arc2D.Float(x + w - 2 * r, y, 2 * r, 2 * r, 90f, -90f, Arc2D.OPEN), true)
            path.lineTo(x + w, y + h - if (br) r else 0f)
            if (br) path.append(Arc2D.Float(x + w - 2 * r, y + h - 2 * r, 2 * r, 2 * r, 0f, -90f, Arc2D.OPEN), true)
            path.lineTo(x + if (bl) r else 0f, y + h)
            if (bl) path.append(Arc2D.Float(x, y + h - 2 * r, 2 * r, 2 * r, 270f, -90f, Arc2D.OPEN), true)
            path.lineTo(x, y + if (tl) r else 0f)
            if (tl) path.append(Arc2D.Float(x, y, 2 * r, 2 * r, 180f, -90f, Arc2D.OPEN), true)
            path.closePath()
            return path
        }
    }
}
*///?}
