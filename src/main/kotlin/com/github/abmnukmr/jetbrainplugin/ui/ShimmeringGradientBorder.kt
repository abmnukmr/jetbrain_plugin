package com.github.abmnukmr.jetbrainplugin.ui

import java.awt.*
import java.awt.geom.Point2D
import java.awt.geom.RoundRectangle2D
import javax.swing.border.AbstractBorder
import kotlin.math.roundToInt
import java.lang.ref.WeakReference
import javax.swing.Timer

class ShimmeringGradientBorder(
    private val thickness: Int = 4,
    private val colors: Array<Color> = arrayOf(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.MAGENTA),
    private val cornerRadius: Int = 16,       // <-- new parameter for border radius
    animationSpeed: Int = 50
) : AbstractBorder() {

    private var offset = 0f
    private var componentRef: WeakReference<Component>? = null
    private val timer = Timer(animationSpeed) {
        offset += 0.02f
        if (offset > 1f) offset = 0f
        componentRef?.get()?.repaint()
    }

    override fun paintBorder(c: Component?, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        if (c == null) return

        if (componentRef == null || componentRef?.get() !== c) {
            componentRef = WeakReference(c)
            if (!timer.isRunning) timer.start()
        }

        val g2 = g as Graphics2D
        val oldPaint = g2.paint

        // Enable anti-aliasing for smooth rounded corners
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val gradientWidth = width / 2f
        val startX = x.toFloat() + gradientWidth * offset

        val fractions = FloatArray(colors.size) { i -> i.toFloat() / (colors.size - 1) }
        val shiftedColors = colors.rotateColors(offset)

        val paint = LinearGradientPaint(
            Point2D.Float(startX, y.toFloat()),
            Point2D.Float(startX + gradientWidth, y.toFloat()),
            fractions,
            shiftedColors
        )

        g2.paint = paint

        // Draw multiple rounded rectangles to make the border thickness
        for (i in 0 until thickness) {
            val rect = RoundRectangle2D.Float(
                x + i.toFloat(),
                y + i.toFloat(),
                (width - 2 * i - 1).toFloat(),
                (height - 2 * i - 1).toFloat(),
                cornerRadius.toFloat(),
                cornerRadius.toFloat()
            )
            g2.draw(rect)
        }

        g2.paint = oldPaint
    }

    override fun getBorderInsets(c: Component?) = Insets(thickness, thickness, thickness, thickness)

    override fun isBorderOpaque() = true

    private fun Array<Color>.rotateColors(offset: Float): Array<Color> {
        val n = size
        val shift = ((offset * n).roundToInt()) % n
        return Array(n) { i -> this[(i + shift) % n] }
    }

    fun createRoundedBorder(color: Color, thickness: Int, radius: Int): AbstractBorder {
        return object : AbstractBorder() {
            override fun paintBorder(c: Component?, g: Graphics?, x: Int, y: Int, width: Int, height: Int) {
                if (g !is Graphics2D) return
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g.color = color
                g.stroke = BasicStroke(thickness.toFloat())
                g.drawRoundRect(x + thickness / 2, y + thickness / 2, width - thickness, height - thickness, radius, radius)
            }
        }
    }
}


