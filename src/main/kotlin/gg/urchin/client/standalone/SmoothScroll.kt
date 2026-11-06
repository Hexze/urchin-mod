//? if = 1.8.9 {
/*package gg.urchin.client.standalone

import kotlin.math.abs
import kotlin.math.min

private const val RAW_PER_NOTCH = 120f
private const val WHEEL_MS = 20f
private const val THUMB_MS = 20f
private const val DEFAULT_MS = 30f
private const val MAX_FRAME_MS = 100f
private const val NANOS_PER_MS = 1_000_000f
private const val SETTLED = 0.01f

// The same smooth scrolling the AUtils menu uses (Badlion's scrollbar algorithm). A position has an `offset`
// and a `pending` distance still to travel. A wheel notch adds to what is pending, scrolling the other way
// first throws away what was pending, and every frame the offset moves by pending * min(1, dt / duration)
// with dt the frame time in milliseconds. Most of a notch is travelled within the first frame or two and the
// rest settles smoothly. Reaching either end stops it.
internal class SmoothScroll(private val perNotch: Float) {
    var offset = 0f
        private set
    private var pending = 0f
    private var duration = DEFAULT_MS
    private var last = System.nanoTime()

    // A wheel turn of `raw` units (positive is up, 120 per notch).
    fun wheel(raw: Float) {
        if (raw == 0f) return
        if ((raw > 0 && pending > 0) || (raw < 0 && pending < 0)) pending = 0f
        pending -= raw / RAW_PER_NOTCH * perNotch
        duration = WHEEL_MS
    }

    // The scrollbar thumb: its target becomes the position and the content follows over 20 ms.
    fun dragTo(target: Float) {
        pending = target - offset
        duration = THUMB_MS
    }

    fun jumpTo(position: Float) {
        offset = position
        pending = 0f
    }

    val moving: Boolean
        get() = abs(pending) > SETTLED

    // Advances by the time since the last call and keeps the offset within [0, max]. Returns whether it moved.
    fun update(max: Float): Boolean {
        val now = System.nanoTime()
        val dt = min(MAX_FRAME_MS, (now - last) / NANOS_PER_MS)
        last = now
        val before = offset
        val step = pending * min(1f, dt / duration)
        offset += step
        pending -= step
        if (abs(pending) < SETTLED) pending = 0f
        if (offset < 0f || max <= 0f) {
            offset = 0f
            pending = 0f
        } else if (offset > max) {
            offset = max
            pending = 0f
        }
        return offset != before
    }
}
*///?}
