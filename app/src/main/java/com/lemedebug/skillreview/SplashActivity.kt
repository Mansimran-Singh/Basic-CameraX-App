package com.lemedebug.skillreview

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.lemedebug.skillreview.databinding.ActivitySplashBinding
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    /**
     * onCreate Method
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Get viewBinding
        val splashBinding: ActivitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)

        //Full Screen, Hide Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        //Set animation defined in anim_splash
        val splashAnimation = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.anim_splash)
        splashBinding.logoSplashScreen.animation = splashAnimation

        //Animation listener to create delay and rotate the logo camera
        splashAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                // Animate the image in the splash screen
                logo_camera.animate().apply {
                    duration = 500 // 0.5 second
                    rotationY(360f) // rotate 360 degrees on Y axis
                }.start()

                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }, 500)
            }
            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
    }
}