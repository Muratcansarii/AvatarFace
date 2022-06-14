
package com.example.avatararvr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


class SplashActivitiy : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_activitiy)

        val logoTimer: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(20)
                } catch (e: InterruptedException) {
                    Log.d("Exception", "Exception$e")
                } finally {
                    val intent = Intent(baseContext, MainActivity::class.java)
                    startActivity(intent)
                }
                finish()
            }
        }
        logoTimer.start()
        /*val webViewFragment = WebViewFragment()
        showFragment(webViewFragment)*/
    }

    /*fun showFragment(fragment: WebViewFragment){
        val fram = supportFragmentManager.beginTransaction()
        fram.replace(R.id.containerFragment,fragment)
        fram.commit()
    }*/
}