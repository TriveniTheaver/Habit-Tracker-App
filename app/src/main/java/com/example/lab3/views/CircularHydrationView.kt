package com.example.lab3.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.example.lab3.R
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom circular progress view for hydration tracking.
 * Shows a large circle that fills with water color as hydration is added.
 */
class CircularHydrationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val remainingTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var progress = 0f // 0-1
    private var currentAmount = 0
    private var targetAmount = 2000
    private var percentage = 0
    
    // Animation
    private var animationTime = 0f
    private var waterAnimator: ValueAnimator? = null
    
    // Dripping animation
    private var isDripping = false
    private var drippingProgress = 0f
    private var drippingAnimator: ValueAnimator? = null
    private val drips = mutableListOf<Drip>()
    
    data class Drip(var x: Float, var y: Float, var alpha: Float, var size: Float)
    
    // Colors - using blue water colors
    private val waterColor = Color.parseColor("#4FC3F7") // Light blue water color
    private val backgroundWaterColor = Color.parseColor("#E1F5FE") // Very light blue background
    private val backgroundColor = Color.WHITE
    private val textColor = ContextCompat.getColor(context, R.color.text_primary)
    private val remainingTextColor = ContextCompat.getColor(context, R.color.text_secondary)
    
    init {
        setupPaints()
        startWaterAnimation()
    }
    
    private fun setupPaints() {
        // Background circle paint
        backgroundPaint.color = backgroundWaterColor
        backgroundPaint.style = Paint.Style.FILL
        
        // Progress (water) paint
        progressPaint.color = waterColor
        progressPaint.style = Paint.Style.FILL
        
        // Circle border paint
        paint.color = backgroundColor
        paint.style = Paint.Style.FILL
        
        // Percentage text paint
        textPaint.color = textColor
        textPaint.textSize = 72f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
        
        // Remaining text paint
        remainingTextPaint.color = remainingTextColor
        remainingTextPaint.textSize = 36f
        remainingTextPaint.textAlign = Paint.Align.CENTER
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        centerX = w / 2f
        centerY = h / 2f
        radius = minOf(w, h) / 2f - 20f // 20dp margin
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw background circle (unfilled water)
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)
        
        // Draw animated water inside the circle
        if (progress > 0) {
            drawAnimatedWater(canvas)
        }
        
        // Draw dripping water animation
        if (isDripping) {
            drawDrippingWater(canvas)
        }
        
        // Draw percentage text (large, directly on water with shadow for visibility)
        val percentageText = "$percentage%"
        textPaint.textSize = 100f // Reduced size for better proportion
        textPaint.color = Color.BLACK
        textPaint.setShadowLayer(6f, 0f, 0f, Color.argb(120, 255, 255, 255))
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val percentageY = centerY - 10f
        canvas.drawText(percentageText, centerX, percentageY, textPaint)
        
        // Draw current/target text below percentage
        val currentTargetText = "${currentAmount}ml / ${targetAmount}ml"
        remainingTextPaint.textSize = 40f // Bigger for better visibility
        remainingTextPaint.color = Color.BLACK
        remainingTextPaint.setShadowLayer(5f, 0f, 0f, Color.argb(120, 255, 255, 255))
        remainingTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val currentTargetY = centerY + 50f
        canvas.drawText(currentTargetText, centerX, currentTargetY, remainingTextPaint)
    }
    
    /**
     * Updates the hydration progress
     * @param current Current hydration amount in ml
     * @param target Target hydration amount in ml
     * @param animate Whether to show dripping animation
     */
    fun updateProgress(current: Int, target: Int, animate: Boolean = false) {
        val oldProgress = this.progress
        this.currentAmount = current
        this.targetAmount = target
        this.progress = if (target > 0) (current.toFloat() / target.toFloat()).coerceAtMost(1f) else 0f
        this.percentage = (progress * 100).toInt()
        
        // Start dripping animation if water was added
        if (animate && current > 0 && progress > oldProgress) {
            startDrippingAnimation()
        }
        
        invalidate()
    }
    
    /**
     * Draws animated water filling the circle from bottom to top
     */
    private fun drawAnimatedWater(canvas: Canvas) {
        // Calculate water level from bottom of circle
        // At 100%, fill completely to the top
        val actualProgress = if (progress >= 1f) 1.0f else progress
        val waterHeight = radius * 2 * actualProgress
        val waterTop = centerY + radius - waterHeight
        
        // Save canvas state for clipping
        canvas.save()
        
        // Create clipping path for the circle
        val clipPath = Path()
        clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW)
        canvas.clipPath(clipPath)
        
        // Draw water filling from bottom to top with wave animation
        val waterPath = Path()
        
        // Calculate wave points for the water surface
        val wavePoints = mutableListOf<PointF>()
        val numPoints = 40
        
        for (i in 0..numPoints) {
            val x = centerX - radius + (radius * 2 * i / numPoints)
            val normalizedX = (x - (centerX - radius)) / (radius * 2)
            
            // Calculate wave height based on animation time - BIGGER WAVES!
            val waveHeight = 20f * (1f + 0.5f * sin(normalizedX * 4 * Math.PI.toFloat() + animationTime))
            val y = waterTop + waveHeight
            
            wavePoints.add(PointF(x, y))
        }
        
        // Create water shape that fills from bottom
        waterPath.moveTo(centerX - radius, centerY + radius)
        
        // Draw bottom part (straight line up to water level)
        waterPath.lineTo(centerX - radius, waterTop)
        
        // Draw animated wave top
        for (i in 1 until wavePoints.size) {
            val prev = wavePoints[i - 1]
            val curr = wavePoints[i]
            
            val controlX = (prev.x + curr.x) / 2
            val controlY = (prev.y + curr.y) / 2
            
            waterPath.quadTo(controlX, prev.y, curr.x, curr.y)
        }
        
        // Complete the water shape
        waterPath.lineTo(centerX + radius, centerY + radius)
        waterPath.close()
        
        // Draw water with gradient for depth effect
        val gradient = LinearGradient(
            0f, waterTop,
            0f, centerY + radius,
            intArrayOf(
                Color.argb(150, Color.red(waterColor), Color.green(waterColor), Color.blue(waterColor)),
                Color.argb(200, Color.red(waterColor), Color.green(waterColor), Color.blue(waterColor)),
                waterColor
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        
        progressPaint.shader = gradient
        canvas.drawPath(waterPath, progressPaint)
        progressPaint.shader = null
        
        // Restore canvas state
        canvas.restore()
    }
    
    /**
     * Starts the water animation
     */
    private fun startWaterAnimation() {
        waterAnimator = ValueAnimator.ofFloat(0f, 2 * Math.PI.toFloat()).apply {
            duration = 2000 // Faster waves for more visible movement
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                animationTime = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
    
    /**
     * Starts the dripping water animation
     */
    private fun startDrippingAnimation() {
        isDripping = true
        drips.clear()
        
        // Create multiple drips at random positions
        for (i in 0..5) {
            val randomX = centerX - radius * 0.5f + (Math.random().toFloat() * radius)
            drips.add(Drip(randomX, centerY - radius, 255f, 10f + Math.random().toFloat() * 10f))
        }
        
        drippingAnimator?.cancel()
        drippingAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            addUpdateListener { animation ->
                drippingProgress = animation.animatedValue as Float
                
                // Update drip positions
                val waterTop = centerY + radius - (radius * 2 * progress)
                for (drip in drips) {
                    drip.y = centerY - radius + (waterTop - (centerY - radius)) * drippingProgress
                    drip.alpha = 255f * (1f - drippingProgress)
                }
                
                invalidate()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    isDripping = false
                    drips.clear()
                    invalidate()
                }
            })
            start()
        }
    }
    
    /**
     * Draws the dripping water animation
     */
    private fun drawDrippingWater(canvas: Canvas) {
        val dripPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dripPaint.color = waterColor
        
        for (drip in drips) {
            dripPaint.alpha = drip.alpha.toInt().coerceIn(0, 255)
            
            // Draw drip as a teardrop shape
            val path = Path()
            path.addCircle(drip.x, drip.y, drip.size, Path.Direction.CW)
            
            // Add tail to drip
            path.moveTo(drip.x, drip.y - drip.size)
            path.lineTo(drip.x - drip.size * 0.3f, drip.y - drip.size * 2)
            path.lineTo(drip.x + drip.size * 0.3f, drip.y - drip.size * 2)
            path.close()
            
            canvas.drawPath(path, dripPaint)
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        waterAnimator?.cancel()
        drippingAnimator?.cancel()
    }
    
    /**
     * Gets the current hydration level for motivational messages
     */
    fun getHydrationLevel(): HydrationLevel {
        return when {
            percentage >= 100 -> HydrationLevel.COMPLETE
            percentage >= 75 -> HydrationLevel.EXCELLENT
            percentage >= 50 -> HydrationLevel.GOOD
            percentage >= 25 -> HydrationLevel.STARTING
            else -> HydrationLevel.LOW
        }
    }
    
    /**
     * Gets the remaining amount to reach target
     */
    fun getRemainingAmount(): Int {
        return maxOf(0, targetAmount - currentAmount)
    }
    
    enum class HydrationLevel {
        LOW, STARTING, GOOD, EXCELLENT, COMPLETE
    }
}
