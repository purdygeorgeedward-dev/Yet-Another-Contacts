package org.fossify.contacts.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import org.fossify.commons.extensions.darkenColor
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.extensions.getNameLetter
import org.fossify.commons.extensions.lightenColor
import kotlin.math.abs

/**
 * A vivid, glossy-sphere palette in the same spirit as Mac OS X's Aqua era -
 * saturated, candy-like accent colors, picked to actually look colorful
 * rather than muted. Same hash-based per-contact selection approach as
 * Commons' own SimpleContactsHelper.getContactLetterIcon() (same contact
 * name always gets the same color), just a different, more vivid palette
 * and gel rendering instead of a flat fill.
 */
private val GEL_AVATAR_PALETTE = intArrayOf(
    Color.parseColor("#0EA5E9"), // sky blue
    Color.parseColor("#10B981"), // emerald green
    Color.parseColor("#EC4899"), // hot pink
    Color.parseColor("#F59E0B"), // amber/orange
    Color.parseColor("#8B5CF6"), // purple
    Color.parseColor("#14B8A6"), // teal
    Color.parseColor("#EF4444"), // coral red
    Color.parseColor("#EAB308")  // gold
)

/**
 * Builds a glossy gel-style circular avatar for a contact with no photo,
 * in the same visual language as the Messages app's gel bubble theme
 * (extensions/GelBubble.kt there) - gradient body, darker rim, soft
 * specular highlight - but drawn on a Canvas rather than as a Drawable,
 * since it needs to composite a letter on top the same way the function
 * it replaces already does.
 *
 * Doesn't touch or wrap Commons' own getContactLetterIcon() - that's
 * compiled library code this app can't modify, and it's still used as-is
 * for the launcher shortcut icon case (Android's own adaptive-icon masking
 * applies there, which isn't something to introduce new gradient/highlight
 * artwork into without being able to verify it on a real launcher).
 */
fun Context.createGelContactAvatar(name: String): Bitmap {
    val letter = name.getNameLetter()
    val size = resources.getDimension(org.fossify.commons.R.dimen.normal_icon_size).toInt()
    val baseColor = GEL_AVATAR_PALETTE[abs(name.hashCode()) % GEL_AVATAR_PALETTE.size]
    val lightColor = baseColor.lightenColor(30)
    val darkColor = baseColor.darkenColor(18)
    val rimColor = baseColor.darkenColor(32)

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = size / 2f

    val bodyPaint = Paint().apply {
        isAntiAlias = true
        shader = LinearGradient(0f, 0f, 0f, size.toFloat(), lightColor, darkColor, Shader.TileMode.CLAMP)
    }
    canvas.drawCircle(center, center, center, bodyPaint)

    val rimStrokeWidth = size / 24f
    val rimPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = rimStrokeWidth
        color = rimColor
    }
    canvas.drawCircle(center, center, center - rimStrokeWidth / 2f, rimPaint)

    // Soft specular highlight, upper-left of center - same visual idea as
    // the Messages gel bubble's highlight blob, adapted to a circle.
    val highlightRadius = size * 0.4f
    val highlightPaint = Paint().apply {
        isAntiAlias = true
        shader = RadialGradient(
            size * 0.35f, size * 0.3f, highlightRadius,
            Color.argb(130, 255, 255, 255), Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawCircle(size * 0.35f, size * 0.3f, highlightRadius, highlightPaint)

    val textPaint = Paint().apply {
        isAntiAlias = true
        color = baseColor.getContrastColor()
        textAlign = Paint.Align.CENTER
        textSize = size / 2f
    }
    val xPos = center
    val yPos = center - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText(letter, xPos, yPos, textPaint)

    return bitmap
}
