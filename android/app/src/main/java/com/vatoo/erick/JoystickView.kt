package com.vatoo.erick

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.vatoo.erick.shared.ColorEntry
import com.vatoo.erick.shared.ColorPaletteType
import com.vatoo.erick.shared.ColorPalettes
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class JoystickView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // --- 兼容旧代码遗留（新架构中已不再使用，仅防报错） ---
    var processor: Any? = null

    // --- 核心状态变量 ---
    var isRightSide: Boolean = false
    private var previewText: String = ""

    // --- 调色板 ---
    // Direction order matches the drawing loop: E, SE, S, SW, W, NW, N, NE
    // which maps to palette positions 0–7
    private var paletteColors: List<ColorEntry> = ColorPalettes.getPalette(ColorPaletteType.DEFAULT)

    // Per-sector Paint objects (reused across draws)
    private val sectorPaints: Array<Paint> = Array(8) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    }

    /** Call this whenever the user changes palette in settings. */
    fun setPalette(type: ColorPaletteType) {
        paletteColors = ColorPalettes.getPalette(type)
        applyPaletteToSectorPaints()
        invalidate()
    }

    private fun applyPaletteToSectorPaints() {
        paletteColors.forEachIndexed { i, entry ->
            // entry.hexColor is 0xFFRRGGBB — Android Color.toArgb() compatible
            sectorPaints[i].color = entry.hexColor.toInt()
            // Lighten slightly so sectors feel like a soft guide, not a full fill
            sectorPaints[i].alpha = 255  // 0–255; ~35% opacity
        }
    }

    // --- 尺寸与坐标 ---
    private var centerX = 0f
    private var centerY = 0f
    private var baseRadius = 0f
    private var thumbRadius = 0f

    // 滑块当前的绝对坐标
    private var thumbX = 0f
    private var thumbY = 0f

    // Reusable drawing objects
    private val sectorPath = Path()
    private val baseOval   = RectF()

    // --- 画笔设置 ---
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        style = Paint.Style.FILL
    }

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#757575")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1976D2")
        textSize = 80f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val directionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BDBDBD")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val directionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9E9E9E")
        textSize = 36f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    init {
        applyPaletteToSectorPaints()
    }

    // --- 尺寸初始化 ---
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX    = w / 2f
        centerY    = h / 2f
        baseRadius = (minOf(w, h) / 2f) * 0.85f
        thumbRadius = baseRadius / 3f
        baseOval.set(
            centerX - baseRadius, centerY - baseRadius,
            centerX + baseRadius, centerY + baseRadius
        )
        resetThumb()
    }

    // --- 核心绘制逻辑 ---
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Base circle
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint)

        // 2. Colored sectors (right joystick only)
        //    Each sector is a 45° pie slice from the deadzone edge to the base edge.
        //    The direction loop order is: E(0°), SE(45°), S(90°) … matching palette positions 0–7.
        if (isRightSide) {
            val deadzoneRadius = baseRadius * 0.30f  // inner radius of the colored ring
            val innerOval = RectF(
                centerX - deadzoneRadius, centerY - deadzoneRadius,
                centerX + deadzoneRadius, centerY + deadzoneRadius
            )

            for (i in 0 until 8) {
                // Each sector spans 45°; we start 22.5° before the direction angle so the
                // label sits in the middle of the sector.
                val sweepDeg   = 45f
                val startDeg   = (i * 45f) - 22.5f   // degrees (0° = 3 o'clock = East)

                sectorPath.reset()
                // Outer arc
                sectorPath.arcTo(baseOval, startDeg, sweepDeg, true)
                // Line to inner arc start
                val endDeg = Math.toRadians((startDeg + sweepDeg).toDouble())
                sectorPath.lineTo(
                    centerX + deadzoneRadius * cos(endDeg).toFloat(),
                    centerY + deadzoneRadius * sin(endDeg).toFloat()
                )
                // Inner arc (reversed)
                sectorPath.arcTo(innerOval, startDeg + sweepDeg, -sweepDeg, false)
                sectorPath.close()

                canvas.drawPath(sectorPath, sectorPaints[i])
            }
        }

        // 3. Direction guide lines and labels
        val dirLabels = arrayOf("E", "SE", "S", "SW", "W", "NW", "N", "NE")
        for (i in 0 until 8) {
            val angle = i * Math.PI / 4

            val innerRadius = baseRadius * 0.3f
            val outerRadius = baseRadius * 0.75f

            val startX = centerX + innerRadius * cos(angle).toFloat()
            val startY = centerY + innerRadius * sin(angle).toFloat()
            val stopX  = centerX + outerRadius * cos(angle).toFloat()
            val stopY  = centerY + outerRadius * sin(angle).toFloat()
            canvas.drawLine(startX, startY, stopX, stopY, directionPaint)

            val textRadius = baseRadius * 0.88f
            val textX = centerX + textRadius * cos(angle).toFloat()
            val textY = centerY + textRadius * sin(angle).toFloat()
            val textOffsetY = (directionTextPaint.descent() + directionTextPaint.ascent()) / 2
            canvas.drawText(dirLabels[i], textX, textY - textOffsetY, directionTextPaint)
        }

        // 4. Preview text (right joystick)
        if (isRightSide && previewText.isNotEmpty()) {
            val textOffsetY = (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(previewText, centerX, centerY - textOffsetY, textPaint)
        }

        // 5. Thumb
        canvas.drawCircle(thumbX, thumbY, thumbRadius, thumbPaint)
    }

    // --- 供 Service 调用的 UI 更新方法 ---

    fun updateThumb(dx: Float, dy: Float) {
        val distance  = hypot(dx.toDouble(), dy.toDouble()).toFloat()
        val maxRadius = baseRadius - thumbRadius
        if (distance > maxRadius) {
            val ratio = maxRadius / distance
            thumbX = centerX + dx * ratio
            thumbY = centerY + dy * ratio
        } else {
            thumbX = centerX + dx
            thumbY = centerY + dy
        }
        invalidate()
    }

    fun resetThumb() {
        thumbX = centerX
        thumbY = centerY
        invalidate()
    }

    fun setPreviewText(text: String) {
        if (previewText != text) {
            previewText = text
            invalidate()
        }
    }
}