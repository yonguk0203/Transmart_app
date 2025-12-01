package com.example.extest

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import android.content.Context

import android.view.Gravity
import android.view.WindowManager

import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout


open class MenubarActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout
    private lateinit var popupLayer: View
    private val pointRate = 0.1
    private val PREFS_NAME = "app_prefs"
    private val KEY_POINTS = "user_points"


    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        val contents = result.contents ?: run {
            Toast.makeText(this, "스캔이 취소되었습니다.", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        val n = parseMoas(contents)
        if (n == null) {
            Toast.makeText(this, "지원하지 않는 QR입니다.", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        val orderId   = "order_test_%03d".format(n)
        val productId = "A%03d".format(n)
        val ref = FirebaseDatabase.getInstance()
            .getReference("orders")
            .child(orderId)

        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val price = snapshot.child("price").getValue(Int::class.java) ?: 0
                val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                val oldPoints = prefs.getInt(KEY_POINTS, 0)
                val earnedPoints = (price * pointRate).toInt()
                val newTotalPoints = oldPoints + earnedPoints


                prefs.edit().putInt(KEY_POINTS, newTotalPoints).apply()

                updatePointUI(newTotalPoints)

                ref.updateChildren(mapOf(
                    "status" to "paid",
                    "product_id" to productId
                )).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "결제 완료: $orderId / 적립 $earnedPoints P", Toast.LENGTH_SHORT).show()

                        val prefs = getSharedPreferences("log_prefs", MODE_PRIVATE)
                        val oldLogs = prefs.getString("paid_logs", "") ?: ""

                        val timestamp = java.text.SimpleDateFormat(
                            "yyyy/MM/dd HH:mm",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date())

                        val newLog = "$productId|$price|$earnedPoints|$timestamp"

                        val updatedLogs = if (oldLogs.isEmpty()) newLog else "$oldLogs\n$newLog"
                        prefs.edit().putString("paid_logs", updatedLogs.trim()).commit()

                        val intent = Intent(this, PaidLogActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Firebase 업데이트 실패", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    private fun parseMoas(text: String): Int? {
        val m = Regex("""^MOAS_(\d)OBJ$""").find(text) ?: return null
        val n = m.groupValues[1].toIntOrNull() ?: return null
        return if (n in 1..6) n else null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun setContentLayout(layoutResId: Int) {

        setContentView(R.layout.activity_menubar)

        layoutInflater.inflate(layoutResId, findViewById(R.id.content_frame))

        initMenuBar()

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val totalPoints = prefs.getInt(KEY_POINTS, 0)
        updatePointUI(totalPoints)
    }

    protected fun updatePointUI(points: Int) {
        val sidebarPointView = findViewById<TextView?>(R.id.sidebarPointText)
        sidebarPointView?.text = "$points"

        val pointDetailView = findViewById<TextView?>(R.id.pointDetailText)
        pointDetailView?.text = "$points"
    }


    protected fun showPopup() {
        popupLayer.visibility = View.VISIBLE
    }

    protected fun hidePopup() {
        popupLayer.visibility = View.GONE
    }

    private fun initMenuBar() {
        drawerLayout = findViewById(R.id.drawerLayout)

        val btnMenu = findViewById<ImageView>(R.id.menu)
        val btnSearch = findViewById<ImageView>(R.id.search)
        val btnHome = findViewById<ImageView>(R.id.home)
        val btnMyPage = findViewById<ImageView>(R.id.mypage)
        val btnpaid = findViewById<ImageView>(R.id.paid)
        val btnCard  = findViewById<ImageView>(R.id.btn_card)
        val btnQr  = findViewById<ImageView>(R.id.btn_qr)
        popupLayer = findViewById(R.id.popup_layer)

        bindPressEffectAndClick(
            btnMenu,
            offRes = R.mipmap.menu_image_nomal,
            onRes = R.mipmap.menu_image_push
        ) {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        bindPressEffectAndClick(
            btnSearch,
            offRes = R.mipmap._search_image_nomal,
            onRes = R.mipmap._search_image_push
        ) {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        bindPressEffectAndClick(
            btnHome,
            offRes = R.mipmap.home_image_nomal,
            onRes = R.mipmap.home_image_push
        ) {
            startActivity(Intent(this, MainActivity::class.java))
        }

        bindPressEffectAndClick(
            btnMyPage,
            offRes = R.mipmap.mypage_image_nomal,
            onRes = R.mipmap.mypage_image_push
        ) {
            startActivity(Intent(this, MYpageActivity::class.java))
        }

        bindPressEffectAndClick(
            btnpaid,
            offRes = R.mipmap.paid_image_nomal,
            onRes = R.mipmap.paid_image_push
        ) {
            showPopup()
        }

        bindPressEffectAndClick(
            btnCard,
            offRes = R.mipmap.button_card_off,
            onRes = R.mipmap.button_card_on
        ) {
            val intent = Intent(this, Barcode_Activity::class.java)
            startActivity(intent)
        }

        bindPressEffectAndClick(
            btnQr,
            offRes = R.mipmap.button_qr_off,
            onRes = R.mipmap.button_qr_on
        ) {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("QR 코드를 스캔하세요")
                setBeepEnabled(true)
                setOrientationLocked(true)
                setBarcodeImageEnabled(false)
            }
            barcodeLauncher.launch(options)
        }

        popupLayer.setOnClickListener {
            hidePopup()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else if (popupLayer.visibility == View.VISIBLE) {
                    hidePopup()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
        initSideBarButtons()
    }

    private fun showConnectAlert() {
        val inflater = layoutInflater
        val popupView = inflater.inflate(R.layout.popup_connect_alert, null)

        val popupWindow = PopupWindow(
            popupView,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0)

        val container = popupWindow.contentView.rootView
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = (container.layoutParams as WindowManager.LayoutParams).apply {
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.6f
        }
        wm.updateViewLayout(container, p)

        val btnYes = popupView.findViewById<ImageView>(R.id.btn_yes)
        btnYes.setOnClickListener {
            popupWindow.dismiss()
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }
    }




    private fun initSideBarButtons() {
        val btnPoint = findViewById<Button>(R.id.btn_point)
        val btnCoupon = findViewById<Button>(R.id.btn_coupon)
        val btnHistory = findViewById<Button>(R.id.btn_paid_log)
        val btnPayment = findViewById<Button>(R.id.btn_payment)
        val btnFavorite = findViewById<Button>(R.id.btn_favorite)
        val btnNotice = findViewById<Button>(R.id.btn_notice)
        val btnFaq = findViewById<Button>(R.id.btn_faq)
        val btnAlram = findViewById<Button>(R.id.btn_alram)
        val btnSetting = findViewById<Button>(R.id.btn_setting)

        btnPoint.setOnClickListener {
            startActivity(Intent(this, PointActivity::class.java )) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnCoupon.setOnClickListener {
            startActivity(Intent(this, CuponActivity::class.java)) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, PaidLogActivity::class.java )) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnPayment.setOnClickListener {
            startActivity(Intent(this, PaidActivity::class.java )) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnFavorite.setOnClickListener {
            startActivity(Intent(this, FavoriteActivity::class.java )) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnNotice.setOnClickListener {
            startActivity(Intent(this, NoticeActivity::class.java )) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnFaq.setOnClickListener {
            startActivity(Intent(this, FAQ_Activity::class.java )) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }



        btnAlram.setOnClickListener {
            startActivity(Intent(this, AlramActivity::class.java )) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnSetting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java )) //
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }


    private fun bindPressEffectAndClick(
        view: ImageView,
        offRes: Int,
        onRes: Int,
        onClick: () -> Unit
    ) {
        view.isClickable = true
        view.isFocusable = true
        view.setImageResource(offRes)

        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> view.setImageResource(onRes)
                MotionEvent.ACTION_UP -> {
                    view.setImageResource(offRes)
                    v.performClick()
                    onClick()
                }
                MotionEvent.ACTION_CANCEL -> view.setImageResource(offRes)
            }
            true
        }
    }
}
