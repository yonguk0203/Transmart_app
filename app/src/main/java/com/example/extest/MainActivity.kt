package com.example.extest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.database.FirebaseDatabase

class MainActivity : MenubarActivity() {

    private lateinit var machine: ImageView
    private lateinit var txtTouch: ImageView
    private lateinit var txtNum: ImageView
    private lateinit var helpImage: ImageView
    private lateinit var btnHelp: Button

    private val PREFS_NAME = "app_prefs"
    private val KEY_CONNECTED = "machine_connected"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activity_main)

        machine = findViewById(R.id.machine_balck)
        txtTouch = findViewById(R.id.tuch_text)
        txtNum = findViewById(R.id.num_001)
        helpImage = findViewById(R.id.help_btn_main)
        btnHelp = findViewById(R.id.btn_help)
        val loadingCircle = findViewById<ProgressBar>(R.id.loading_circle)

        resetToDefaultUI()
        applyConnectionUI()

        btnHelp.setOnClickListener {
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val connected = prefs.getBoolean(KEY_CONNECTED, false)

            if (connected) {
                startActivity(Intent(this, APP_OrderActivity::class.java))
            } else {
                startActivity(Intent(this, HelpActivity::class.java))
            }
        }

        machine.setOnClickListener {

            val programRef = FirebaseDatabase.getInstance()
                .getReference("device/program_status")

            programRef.get().addOnSuccessListener { snapshot ->
                val programStatus = snapshot.getValue(String::class.java) ?: "OFF"

                if (programStatus != "ON") {
                    showProgramOffPopup()
                    return@addOnSuccessListener
                }
                val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                val isConnected = prefs.getBoolean(KEY_CONNECTED, false)

                if (!isConnected) {
                    loadingCircle.visibility = View.VISIBLE

                    Handler(Looper.getMainLooper()).postDelayed({
                        loadingCircle.visibility = View.GONE
                        showConnectPopup()
                    }, 1000)

                } else {
                    showDisconnectPopup()
                }
            }
        }
    }
    private fun showProgramOffPopup() {
        val inflater = layoutInflater
        val popupView = inflater.inflate(R.layout.popup_connect_alert, null)

        val popupWindow = PopupWindow(
            popupView,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0)

        val btnYes = popupView.findViewById<ImageView>(R.id.btn_yes)

        btnYes.setOnClickListener {
            popupWindow.dismiss()
            startActivity(Intent(this, MapsActivity::class.java))
        }
    }
    private fun applyConnectionUI() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isConnected = prefs.getBoolean(KEY_CONNECTED, false)

        if (isConnected) {
            helpImage.setImageResource(R.mipmap.apporder_btn_main)
        } else {
            helpImage.setImageResource(R.mipmap.help_btn_main)
        }
    }
    private fun showConnectPopup() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_connect_machine, null)

        val popupWindow = PopupWindow(
            popupView,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0)

        val btnYes = popupView.findViewById<ImageView>(R.id.main_yes_btn)
        val btnNo = popupView.findViewById<ImageView>(R.id.main_no_btn)

        btnYes.setOnClickListener {
            // 🔹 연결 상태 저장
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_CONNECTED, true).apply()

            // 🔹 UI 변경
            machine.setImageResource(R.mipmap.macin_color)
            txtTouch.setImageResource(R.mipmap.connect)
            txtNum.visibility = View.VISIBLE

            applyConnectionUI()
            popupWindow.dismiss()
        }

        btnNo.setOnClickListener {
            popupWindow.dismiss()
        }
    }
    private fun showDisconnectPopup() {
        val inflater = layoutInflater
        val popupView = inflater.inflate(R.layout.popup_disconnect_machine, null)

        val popupWindow = PopupWindow(
            popupView,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0)

        val btnYes = popupView.findViewById<ImageView>(R.id.dis_yes_btn)
        val btnNo = popupView.findViewById<ImageView>(R.id.dis_no_btn)

        btnYes.setOnClickListener {
            disconnectMachine()
            popupWindow.dismiss()
        }

        btnNo.setOnClickListener {
            popupWindow.dismiss()
        }
    }
    private fun disconnectMachine() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_CONNECTED, false).apply()

        machine.setImageResource(R.mipmap.macin_black)
        txtTouch.setImageResource(R.mipmap.touch)
        txtNum.visibility = View.GONE

        applyConnectionUI()

        Toast.makeText(this, "자판기 연결 해제.", Toast.LENGTH_SHORT).show()
    }
    private fun resetToDefaultUI() {
        machine.setImageResource(R.mipmap.macin_black)
        txtTouch.setImageResource(R.mipmap.touch)
        txtNum.visibility = View.GONE
    }
}
