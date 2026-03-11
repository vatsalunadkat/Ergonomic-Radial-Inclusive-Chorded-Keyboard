package com.vatoo.erick

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.vatoo.erick.shared.ColorPalettes
import com.vatoo.erick.shared.Direction
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class JoystickView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var processor: Any? = null
    var isRightSide: Boolean = false
    private var previewText: String = ""

    private var centerX = 0f
    private var centerY = 0f
    private var baseRadius = 0f
    private var thumbRadius = 0f
    private var thumbX = 0f
    private var thumbY = 0f

    var activeDirection: Direction = Direction.NONE
        private set

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#121212") // Dark background for the dial base
        style = Paint.Style.FILL
    }
    
    private val activeSegmentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val segmentLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#121212") // Use base background for block seams
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFCA28") // Golden/Orange outer rim
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9E9E9E")
        style = Paint.Style.FILL
        setShadowLayer(8f, 0f, 4f, Color.BLACK)
    }

    private val thumbInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#757575")
        style = Paint.Style.FILL
    }

    private val charTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 34f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    //'?' is only a placeholder
    private val leftChars = mapOf(
        Direction.N to listOf("a", "b", "c", "d", "e", "?", "?", "'"),
        Direction.NE to listOf("f", "g", "h", "i", "j", "?", "?", "/"),
        Direction.E to listOf("k", "l", "m", "n", "o", "?", "?", ";"),
        Direction.SE to listOf("p", "q", "r", "s", "t", "?", "?", "-"),
        Direction.S to listOf("u", "v", "w", "x", "y", "?", "?", "="),
        Direction.SW to listOf("z", "\\", "[", "]", "`", "?", "?", "?"),
        Direction.W to listOf("1", "2", "3", "4", "5", "?", "?", "?"),
        Direction.NW to listOf("6", "7", "8", "9", "0", "?", "?", "?")
    )
    
    private val rightDirs = listOf(
        Direction.N, Direction.NE, Direction.E, Direction.SE, 
        Direction.S, Direction.SW, Direction.W, Direction.NW
    )

    private val directions = listOf(
        Direction.E, Direction.SE, Direction.S, Direction.SW,
        Direction.W, Direction.NW, Direction.N, Direction.NE
    )

    init {
        // Enable hardware acceleration/shadows
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        baseRadius = (Math.min(w, h) / 2f) * 0.90f
        thumbRadius = baseRadius * 0.22f
        resetThumb()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isRightSide) {
            // Right Dial: 8 solid colored segments
            val rectF = RectF(centerX - baseRadius, centerY - baseRadius, centerX + baseRadius, centerY + baseRadius)

            for (i in 0 until 8) {
                val startAngle = -22.5f + i * 45f
                val sweepAngle = 45f
                val dir = directions[i]
                val isActive = (dir == activeDirection && activeDirection != Direction.NONE)

                activeSegmentPaint.color = Color.parseColor(ColorPalettes.getColorForDirectionHex(dir))
                activeSegmentPaint.alpha = if (activeDirection != Direction.NONE && !isActive) 120 else 255

                canvas.drawArc(rectF, startAngle, sweepAngle, true, activeSegmentPaint)
                canvas.drawArc(rectF, startAngle, sweepAngle, true, segmentLinePaint)
            }
        } else {
            // Left Dial: 3 Concentric Layers with discrete blocks
            val innerHoleRadius = thumbRadius * 1.5f 
            val layerThickness = (baseRadius - innerHoleRadius) / 3f
            val r1 = innerHoleRadius + layerThickness
            val r2 = innerHoleRadius + layerThickness * 2f
            
            val rectFOuter = RectF(centerX - baseRadius, centerY - baseRadius, centerX + baseRadius, centerY + baseRadius)
            val rectFMiddle = RectF(centerX - r2, centerY - r2, centerX + r2, centerY + r2)
            val rectFInner = RectF(centerX - r1, centerY - r1, centerX + r1, centerY + r1)

            // Draw Base Background
            canvas.drawCircle(centerX, centerY, baseRadius, basePaint)

            // Overlapping Pie Slices Drawing Method
            // 1. Draw Outer Layer
            for (i in 0 until 8) {
                val dir = directions[i]
                val startAngle = -22.5f + i * 45f
                val isActive = (dir == activeDirection && activeDirection != Direction.NONE)
                val alphaVal = if (activeDirection != Direction.NONE && !isActive) 100 else 255

                for(j in 0 until 3) {
                    val blockStart = startAngle + j * 15f
                    val colorHex = ColorPalettes.getColorForDirectionHex(rightDirs[j])
                    activeSegmentPaint.color = Color.parseColor(colorHex)
                    activeSegmentPaint.alpha = alphaVal
                    canvas.drawArc(rectFOuter, blockStart, 15f, true, activeSegmentPaint)
                }
            }

            // 2. Draw Middle Layer
            for (i in 0 until 8) {
                val dir = directions[i]
                val startAngle = -22.5f + i * 45f
                val isActive = (dir == activeDirection && activeDirection != Direction.NONE)
                val alphaVal = if (activeDirection != Direction.NONE && !isActive) 100 else 255

                for(j in 0 until 3) {
                    val blockStart = startAngle + j * 15f
                    val colorHex = ColorPalettes.getColorForDirectionHex(rightDirs[3 + j])
                    activeSegmentPaint.color = Color.parseColor(colorHex)
                    activeSegmentPaint.alpha = alphaVal
                    canvas.drawArc(rectFMiddle, blockStart, 15f, true, activeSegmentPaint)
                }
            }

            // 3. Draw Inner Layer
            for (i in 0 until 8) {
                val dir = directions[i]
                val startAngle = -22.5f + i * 45f
                val isActive = (dir == activeDirection && activeDirection != Direction.NONE)
                val alphaVal = if (activeDirection != Direction.NONE && !isActive) 100 else 255

                for(j in 0 until 2) {
                    val blockStart = startAngle + j * 22.5f
                    val colorHex = ColorPalettes.getColorForDirectionHex(rightDirs[6 + j])
                    activeSegmentPaint.color = Color.parseColor(colorHex)
                    activeSegmentPaint.alpha = alphaVal
                    canvas.drawArc(rectFInner, blockStart, 22.5f, true, activeSegmentPaint)
                }
            }

            // 4. Draw Center Hole to cut out the inner pie tips
            canvas.drawCircle(centerX, centerY, innerHoleRadius, basePaint)

            // 5. Draw Separator Circles (Seams between layers)
            canvas.drawCircle(centerX, centerY, r2, segmentLinePaint)
            canvas.drawCircle(centerX, centerY, r1, segmentLinePaint)

            // 6. Draw Separator Lines (Seams within layers)
            for (i in 0 until 8) {
                val startAngle = -22.5f + i * 45f
                
                // Outer/Middle Layer radial separators (every 15 degrees)
                for (j in 0..3) {
                    val angleRad = Math.toRadians((startAngle + j * 15f).toDouble())
                    // If exactly at 0 or 45, it separates main directions, so draw all the way to innerHole. 
                    // Otherwise it separates blocks inside a direction, so draw to r1 (Middle layer bottom).
                    val rStart = if (j == 0 || j == 3) innerHoleRadius else r1
                    val ex = centerX + cos(angleRad).toFloat() * baseRadius
                    val ey = centerY + sin(angleRad).toFloat() * baseRadius
                    val sx = centerX + cos(angleRad).toFloat() * rStart
                    val sy = centerY + sin(angleRad).toFloat() * rStart
                    canvas.drawLine(sx, sy, ex, ey, segmentLinePaint)
                }
                
                // Inner Layer radial separator (at 22.5 degrees)
                val angleRad2 = Math.toRadians((startAngle + 22.5f).toDouble())
                val sx2 = centerX + cos(angleRad2).toFloat() * innerHoleRadius
                val sy2 = centerY + sin(angleRad2).toFloat() * innerHoleRadius
                val ex2 = centerX + cos(angleRad2).toFloat() * r1
                val ey2 = centerY + sin(angleRad2).toFloat() * r1
                canvas.drawLine(sx2, sy2, ex2, ey2, segmentLinePaint)
            }
            
            // 7. Draw outer golden border & inner base limit
            canvas.drawCircle(centerX, centerY, baseRadius, borderPaint)
            canvas.drawCircle(centerX, centerY, innerHoleRadius, segmentLinePaint)

            // 8. Draw Characters
            for (i in 0 until 8) {
                val dir = directions[i]
                val startAngle = -22.5f + i * 45f
                val chars = leftChars[dir] ?: emptyList()
                val isActive = (dir == activeDirection && activeDirection != Direction.NONE)
                val alphaVal = if (activeDirection != Direction.NONE && !isActive) 100 else 255
                
                // Outer Text
                for(j in 0 until 3) {
                    val c = chars.getOrNull(j) ?: continue
                    if (c == "*" || c == "?") continue
                    val colorHex = ColorPalettes.getColorForDirectionHex(rightDirs[j])
                    val centerRad = (baseRadius + r2) / 2f
                    val angleRad = Math.toRadians((startAngle + j * 15f + 7.5f).toDouble())
                    drawCharText(canvas, c, colorHex, centerRad, angleRad, alphaVal)
                }
                // Middle Text
                for(j in 0 until 3) {
                    val c = chars.getOrNull(3 + j) ?: continue
                    if (c == "*" || c == "?") continue
                    val colorHex = ColorPalettes.getColorForDirectionHex(rightDirs[3 + j])
                    val centerRad = (r2 + r1) / 2f
                    val angleRad = Math.toRadians((startAngle + j * 15f + 7.5f).toDouble())
                    drawCharText(canvas, c, colorHex, centerRad, angleRad, alphaVal)
                }
                // Inner Text
                for(j in 0 until 2) {
                    val c = chars.getOrNull(6 + j) ?: continue
                    if (c == "*" || c == "?") continue
                    val colorHex = ColorPalettes.getColorForDirectionHex(rightDirs[6 + j])
                    val centerRad = (r1 + innerHoleRadius) / 2f
                    // 22.5 degree width => center is +11.25
                    val angleRad = Math.toRadians((startAngle + j * 22.5f + 11.25f).toDouble())
                    drawCharText(canvas, c, colorHex, centerRad, angleRad, alphaVal)
                }
            }
        }

        // Draw thumb
        canvas.drawCircle(thumbX, thumbY, thumbRadius, thumbPaint)
        canvas.drawCircle(thumbX, thumbY, thumbRadius * 0.6f, thumbInnerPaint)
    }

    private fun drawCharText(canvas: Canvas, charStr: String, colorHex: String, radiusPos: Float, angleRad: Double, alphaVal: Int) {
        charTextPaint.alpha = alphaVal
        // Make text black if on Yellow block, otherwise white
        charTextPaint.color = if (colorHex.equals("#FBC02D", ignoreCase = true)) Color.BLACK else Color.WHITE 
        
        val charX = centerX + cos(angleRad).toFloat() * radiusPos
        val charY = centerY + sin(angleRad).toFloat() * radiusPos - (charTextPaint.descent() + charTextPaint.ascent()) / 2f
        canvas.drawText(charStr, charX, charY, charTextPaint)
    }

    fun updateThumb(dx: Float, dy: Float) {
        val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
        val maxRadius = if (isRightSide) {
            baseRadius * 0.3f 
        } else {
            baseRadius * 0.4f 
        }

        if (distance > maxRadius) {
            val ratio = maxRadius / distance
            thumbX = centerX + (dx * ratio)
            thumbY = centerY + (dy * ratio)
        } else {
            thumbX = centerX + dx
            thumbY = centerY + dy
        }

        if (distance > baseRadius * 0.15f) {
            val radians = atan2(dy.toDouble(), dx.toDouble())
            var degrees = Math.toDegrees(radians)
            if (degrees < 0) degrees += 360.0

            activeDirection = when {
                degrees >= 337.5 || degrees < 22.5 -> Direction.E
                degrees >= 22.5 && degrees < 67.5 -> Direction.SE
                degrees >= 67.5 && degrees < 112.5 -> Direction.S
                degrees >= 112.5 && degrees < 157.5 -> Direction.SW
                degrees >= 157.5 && degrees < 202.5 -> Direction.W
                degrees >= 202.5 && degrees < 247.5 -> Direction.NW
                degrees >= 247.5 && degrees < 292.5 -> Direction.N
                degrees >= 292.5 && degrees < 337.5 -> Direction.NE
                else -> Direction.NONE
            }
        } else {
            activeDirection = Direction.NONE
        }

        invalidate()
    }

    fun resetThumb() {
        thumbX = centerX
        thumbY = centerY
        activeDirection = Direction.NONE
        invalidate()
    }

    fun setPreviewText(text: String) {
        if (previewText != text) {
            previewText = text
            invalidate()
        }
    }
}
