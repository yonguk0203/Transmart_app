package com.example.extest

import android.view.View
import android.widget.ImageView
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback

class PaidLogActivity : MenubarActivity() {

    // ✅ 상품 ID → 이름 매핑
    private val productNameMap = mapOf(
        "A001" to "코카콜라 제로",
        "A002" to "마이구미",
        "A003" to "토레타",
        "A004" to "보조배터리",
        "A005" to "동양미래 굿즈",
        "A006" to "물티슈"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activity_paidlog)

        val prefs = getSharedPreferences("log_prefs", MODE_PRIVATE)
        val existingValue = prefs.all["paid_logs"]
        if (existingValue is Set<*>) {
            prefs.edit().remove("paid_logs").apply()
            android.util.Log.w("PaidLogActivity", "HashSet 형태 로그를 초기화.")
        }

        val backBtn = findViewById<Button>(R.id.btn_back)
        backBtn?.setOnClickListener { goToMain() }

        val container = findViewById<LinearLayout>(R.id.purchaseContainer)
        val imageView14 = findViewById<ImageView>(R.id.imageView14)

        val logsString = prefs.getString("paid_logs", "")?.trim() ?: ""

        if (logsString.isEmpty()) {
            imageView14.visibility = View.VISIBLE
        } else {
            imageView14.visibility = View.GONE

            val logs = logsString.split("\n").filter { it.contains("|") && it.isNotBlank() }

            logs.asReversed().forEach { log ->
                val parts = log.split("|")
                if (parts.size >= 3) {
                    val productId = parts[0]
                    val price = parts[1].toIntOrNull() ?: 0
                    val point = parts[2].toIntOrNull() ?: 0
                    val dateTime = if (parts.size >= 4) parts[3] else ""

                    // ✅ 상품명으로 변환 (없으면 ID 그대로 표시)
                    val productName = productNameMap[productId] ?: productId

                    addPurchaseView(container, productName, price, point, dateTime)
                }
            }
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

    private fun addPurchaseView(container: LinearLayout, productName: String, price: Int, point: Int, dateTime: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.rounded_box)
            setPadding(30, 25, 30, 25)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 12, 0, 12)
            layoutParams = params
            elevation = 6f
        }

        val titleView = TextView(this).apply {
            text = productName
            textSize = 17f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val priceView = TextView(this).apply {
            text = "₩${String.format("%,d", price)}  (+${point}P)"
            textSize = 15f
            setTextColor(Color.parseColor("#CCCCCC"))
        }

        val timeView = TextView(this).apply {
            text = dateTime
            textSize = 13f
            setTextColor(Color.parseColor("#AAAAAA"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            textAlignment = View.TEXT_ALIGNMENT_TEXT_END
        }

        bottomRow.addView(priceView)
        bottomRow.addView(timeView)

        box.addView(titleView)
        box.addView(bottomRow)
        container.addView(box, 0)
    }
}
