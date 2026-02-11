package com.example.lab3

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.lab3.databinding.ActivitySplashBinding

/**
 * Splash Screen Activity - Initial loading screen for the Wellness Tracker application.
 * 
 * This activity serves as the entry point of the application and provides:
 * - Branded splash screen with app logo and name
 * - Brief loading period to allow app initialization
 * - Smooth transition to the main application
 * - Professional first impression for users
 * 
 * The splash screen:
 * - Displays for 2 seconds to show branding
 * - Uses Handler and Looper for delayed navigation
 * - Automatically transitions to MainActivity
 * - Provides smooth user experience on app launch
 * 
 * Implementation details:
 * - Uses view binding for layout access
 * - Implements proper activity lifecycle management
 * - Handles navigation with explicit intents
 * - Finishes itself after navigation to prevent back stack issues
 */
class SplashActivity : AppCompatActivity() {
    
    // View binding for the splash activity layout
    private lateinit var binding: ActivitySplashBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Show splash screen for 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startMainActivity()
        }, 2000)
    }
    
    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
