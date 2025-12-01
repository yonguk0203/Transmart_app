package com.example.extest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.OnBackPressedCallback

class SettingActivity : MenubarActivity() {
    private val PREFS_NAME = "app_prefs"
    private val KEY_POINTS = "user_points"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activitiy_setting)

        val backBtn = findViewById<Button>(R.id.btn_back)
        backBtn?.setOnClickListener {
            goToMain()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToMain()
            }
        })

        val resetBtn = findViewById<Button>(R.id.btn_setup)
        resetBtn?.setOnClickListener {
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            prefs.edit().putInt(KEY_POINTS, 0).apply()
            updatePointUI(0)

            val logPrefs = getSharedPreferences("log_prefs", MODE_PRIVATE)
            logPrefs.edit().remove("paid_logs").apply()

            android.widget.Toast.makeText(this, "포인트 및 결제 로그가 초기화되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}
