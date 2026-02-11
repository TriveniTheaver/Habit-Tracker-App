package com.example.lab3.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.lab3.R
import kotlin.math.sin

class WaterLevelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    
    private var waterLevel = 0f // 0.0 to 1.0
    private var waveOffset = 0f
    private var animationSpeed = 0.02f
    
    private val waterColor = ContextCompat.getColor(context, R.color.secondary)
    private val waveColor = ContextCompat.getColor(context, R.color.primary)
    
    private var animator: ValueAnimator? = null
    
    init {
        setupPaints()
        startWaveAnimation()
    }
    
    private fun setupPaints() {
        paint.color = waterColor
        paint.style = Paint.Style.FILL
        
        wavePaint.color = waveColor
        wavePaint.style = Paint.Style.FILL
        wavePaint.alpha = 150
    }
    
    fun setWaterLevel(level: Float) {
        val clampedLevel = level.coerceIn(0f, 1f)
        if (waterLevel != clampedLevel) {
            waterLevel = clampedLevel
            animateWaterLevel()
        }
    }
    
    private fun animateWaterLevel() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(waterLevel, waterLevel).apply {
            duration = 1000
            addUpdateListener { animation ->
                val currentLevel = animation.animatedValue as Float
                waterLevel = currentLevel
                invalidate()
            }
            start()
        }
    }
    
    private fun startWaveAnimation() {
        val waveAnimator = ValueAnimator.ofFloat(0f, 2 * Math.PI.toFloat())
        waveAnimator.duration = 3000
        waveAnimator.repeatCount = ValueAnimator.INFINITE
        waveAnimator.addUpdateListener { animation ->
            waveOffset = animation.animatedValue as Float
            invalidate()
        }
        waveAnimator.start()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (waterLevel <= 0f) return
        
        val canvasWidth = width.toFloat()
        val canvasHeight = height.toFloat()
        val waterHeight = canvasHeight * waterLevel
        val waterY = canvasHeight - waterHeight
        
        // Draw water background
        canvas.drawRect(0f, waterY, canvasWidth, canvasHeight, paint)
        
        // Draw wave effect
        drawWaves(canvas, canvasWidth, waterY, waterHeight)
        
        // Draw bubbles
        drawBubbles(canvas, canvasWidth, waterY, waterHeight)
    }
    
    private fun drawWaves(canvas: Canvas, canvasWidth: Float, waterY: Float, waterHeight: Float) {
        if (waterHeight < 20f) return
        
        path.reset()
        path.moveTo(0f, waterY)
        
        val waveCount = 3
        val waveAmplitude = 15f
        val waveLength = canvasWidth / waveCount
        
        for (i in 0..(canvasWidth.toInt() / 10)) {
            val x = i * 10f
            val wave1 = (sin((x / waveLength + waveOffset) * 2 * Math.PI) * waveAmplitude).toFloat()
            val wave2 = (sin((x / (waveLength * 0.7f) + waveOffset * 1.3f) * 2 * Math.PI) * waveAmplitude * 0.5f).toFloat()
            val y = waterY + wave1 + wave2
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        path.lineTo(canvasWidth, waterY)
        path.lineTo(canvasWidth, height.toFloat())
        path.lineTo(0f, height.toFloat())
        path.close()
        
        canvas.drawPath(path, wavePaint)
    }
    
    private fun drawBubbles(canvas: Canvas, canvasWidth: Float, waterY: Float, waterHeight: Float) {
        if (waterHeight < 30f) return
        
        val bubbleCount = (waterHeight / 50f).toInt().coerceAtMost(8)
        
        for (i in 0 until bubbleCount) {
            val bubbleX = (canvasWidth * (0.1f + 0.8f * (i % 3) / 2f) + (waveOffset * 10f * (i % 2))).toFloat()
            val bubbleY = waterY + (waterHeight * (0.2f + 0.6f * (i % 4) / 3f))
            val bubbleSize = (5f + (i % 3) * 3f).toFloat()
            
            if (bubbleX in 0f..canvasWidth && bubbleY in waterY..(waterY + waterHeight)) {
                paint.color = Color.WHITE
                paint.alpha = 100
                canvas.drawCircle(bubbleX, bubbleY, bubbleSize, paint)
            }
        }
        
        // Reset paint color
        paint.color = waterColor
        paint.alpha = 255
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}