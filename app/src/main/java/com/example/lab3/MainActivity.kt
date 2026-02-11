package com.example.lab3

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.lab3.databinding.ActivityMainBinding

/**
 * Main Activity for the Wellness Tracker application.
 * 
 * This activity serves as the main container for the app's navigation system.
 * It handles:
 * - Theme preference management (dark/light mode)
 * - Navigation setup with bottom navigation
 * - Edge-to-edge display configuration
 * - Window insets handling for modern Android UI
 * 
 * The activity uses Navigation Component for fragment management and
 * maintains user theme preferences across app sessions.
 */
class MainActivity : AppCompatActivity() {
    
    // View binding for the main activity layout
    private lateinit var binding: ActivityMainBinding
    
    /**
     * Called when the activity is first created.
     * Sets up the main UI components and applies user preferences.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        applyThemePreference()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            insets
        }
    }
    
    private fun applyThemePreference() {
        val sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)
        binding.bottomNavigation.isDrawingCacheEnabled = true
        binding.bottomNavigation.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
    }
}