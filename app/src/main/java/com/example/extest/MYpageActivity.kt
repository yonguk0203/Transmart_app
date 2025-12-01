package com.example.extest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat

class MYpageActivity : MenubarActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activity_mypage)


        val backBtn = findViewById<Button>(R.id.btn_back)
        backBtn?.setOnClickListener {
            goToMain()
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToMain()
            }
        })
    }


    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun initSideBarButtons() {
        val btnSetting = findViewById<Button>(R.id.btn_setting)
        val btnCoupon = findViewById<Button>(R.id.btn_coupon)
        val btnHistory = findViewById<Button>(R.id.btn_paid_log)
        val btnPaid = findViewById<Button>(R.id.btn_paid)
        val btnFavorite = findViewById<Button>(R.id.btn_favorite)


        btnSetting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java)) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnCoupon.setOnClickListener {
            startActivity(Intent(this, CuponActivity::class.java)) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, PaidLogActivity::class.java)) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnPaid.setOnClickListener {
            startActivity(Intent(this, PaidActivity::class.java)) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnFavorite.setOnClickListener {
            startActivity(Intent(this, FavoriteActivity::class.java)) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
}
