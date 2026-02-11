package com.example.lab3.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.lab3.R
import kotlin.math.sin
import kotlin.math.cos

class WaterBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    
    private var waterLevel = 0f // 0.0 to 1.0
    private var waveOffset = 0f
    private var bubbleOffset = 0f
    private var lastInvalidateTime = 0L
    
    // Water colors - Lighter, more pastel blues
    private val waterColor1 = Color.parseColor("#B3E5FC") // Light sky blue
    private val waterColor2 = Color.parseColor("#81D4FA") // Light blue
    private val waterColor3 = Color.parseColor("#4FC3F7") // Light cyan
    
    private var animator: ValueAnimator? = null
    private var waveAnimator: ValueAnimator? = null
    private var bubbleAnimator: ValueAnimator? = null
    
    init {
        setupPaints()
        startAnimations()
    }
    
    private fun setupPaints() {
        // Water gradient paint
        waterPaint.style = Paint.Style.FILL
        
        // Wave paint
        wavePaint.color = Color.WHITE
        wavePaint.alpha = 80
        wavePaint.style = Paint.Style.FILL
        
        // Bubble paint
        bubblePaint.color = Color.WHITE
        bubblePaint.alpha = 120
        bubblePaint.style = Paint.Style.FILL
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
            duration = 1500
            addUpdateListener { animation ->
                val currentLevel = animation.animatedValue as Float
                waterLevel = currentLevel
                invalidate()
            }
            start()
        }
    }
    
    private fun startAnimations() {
        // Wave animation - more movement for better effect
        waveAnimator = ValueAnimator.ofFloat(0f, 2 * Math.PI.toFloat())
        waveAnimator?.duration = 8000 // Faster for more movement
        waveAnimator?.repeatCount = ValueAnimator.INFINITE
        waveAnimator?.addUpdateListener { animation ->
            waveOffset = animation.animatedValue as Float
            // Only invalidate if view is visible and attached, with throttling
            if (isAttachedToWindow && visibility == View.VISIBLE) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastInvalidateTime > 50) { // Throttle to max 20 FPS
                    lastInvalidateTime = currentTime
                    invalidate()
                }
            }
        }
        waveAnimator?.start()
        
        // Bubble animation - more movement for better effect
        bubbleAnimator = ValueAnimator.ofFloat(0f, 2 * Math.PI.toFloat())
        bubbleAnimator?.duration = 10000 // Faster bubble animation
        bubbleAnimator?.repeatCount = ValueAnimator.INFINITE
        bubbleAnimator?.addUpdateListener { animation ->
            bubbleOffset = animation.animatedValue as Float
            // Only invalidate if view is visible and attached, with throttling
            if (isAttachedToWindow && visibility == View.VISIBLE) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastInvalidateTime > 50) { // Throttle to max 20 FPS
                    lastInvalidateTime = currentTime
                    invalidate()
                }
            }
        }
        bubbleAnimator?.start()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (waterLevel <= 0f) return
        
        val width = width.toFloat()
        val height = height.toFloat()
        val waterHeight = height * waterLevel
        val waterY = height - waterHeight
        
        // Draw water background with gradient
        drawWaterBackground(canvas, width, waterY, waterHeight)
        
        // Draw animated waves
        drawWaves(canvas, width, waterY, waterHeight)
        
        // Draw bubbles
        drawBubbles(canvas, width, waterY, waterHeight)
    }
    
    private fun drawWaterBackground(canvas: Canvas, width: Float, waterY: Float, waterHeight: Float) {
        // Use lighter blue color for water background
        waterPaint.reset()
        waterPaint.isAntiAlias = true
        waterPaint.style = Paint.Style.FILL
        waterPaint.color = Color.parseColor("#81D4FA") // Light blue
        waterPaint.shader = null // Remove any shader issues
        
        canvas.drawRect(0f, waterY, width, waterY + waterHeight, waterPaint)
    }
    
    private fun drawWaves(canvas: Canvas, width: Float, waterY: Float, waterHeight: Float) {
        if (waterHeight < 20f) return
        
        path.reset()
        path.moveTo(0f, waterY)
        
        // Create more dynamic waves for better movement
        val amplitude = 8f // Larger amplitude for more visible waves
        val frequency = 0.005f // Higher frequency for more waves
        val waveSpeed = 0.5f // Faster wave movement
        
        for (x in 0..width.toInt() step 4) {
            val wave = sin((x * frequency + waveOffset * waveSpeed) * 2 * Math.PI) * amplitude
            val y = waterY + wave
            
            if (x == 0) {
                path.moveTo(x.toFloat(), y.toFloat())
            } else {
                path.lineTo(x.toFloat(), y.toFloat())
            }
        }
        
        // Complete the wave path - deeper wave for more movement
        path.lineTo(width, waterY)
        path.lineTo(width, waterY + 8f) // Deeper wave depth
        path.lineTo(0f, waterY + 8f)
        path.close()
        
        // Draw the wave in lighter blue
        wavePaint.alpha = 120
        wavePaint.color = Color.parseColor("#4FC3F7") // Light cyan for wave
        wavePaint.shader = null
        canvas.drawPath(path, wavePaint)
    }
    
    private fun drawBubbles(canvas: Canvas, width: Float, waterY: Float, waterHeight: Float) {
        if (waterHeight < 50f) return
        
        val bubbleCount = (waterHeight / 40f).toInt().coerceAtMost(12) // More bubbles for more movement
        
        for (i in 0 until bubbleCount) {
            val bubbleX = (width * (0.1f + 0.8f * (i % 5) / 4f) + (bubbleOffset * 40f * (i % 3))).toFloat()
            val bubbleY = waterY + (waterHeight * (0.1f + 0.8f * (i % 7) / 6f))
            val bubbleSize = (3f + (i % 4) * 2f).toFloat()
            
            if (bubbleX in 0f..width && bubbleY in waterY..(waterY + waterHeight)) {
                bubblePaint.alpha = 100 + (i % 3) * 20
                canvas.drawCircle(bubbleX, bubbleY, bubbleSize, bubblePaint)
                
                // Add smaller bubbles around main bubble
                if (i % 3 == 0) {
                    canvas.drawCircle(bubbleX + bubbleSize, bubbleY - bubbleSize, bubbleSize * 0.5f, bubblePaint)
                    canvas.drawCircle(bubbleX - bubbleSize, bubbleY + bubbleSize, bubbleSize * 0.3f, bubblePaint)
                }
            }
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        waveAnimator?.cancel()
        bubbleAnimator?.cancel()
    }
    
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != View.VISIBLE) {
            // Pause animations when not visible
            waveAnimator?.pause()
            bubbleAnimator?.pause()
        } else {
            // Resume animations when visible
            waveAnimator?.resume()
            bubbleAnimator?.resume()
        }
    }
}
