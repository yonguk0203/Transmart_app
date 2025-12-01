package com.example.extest

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class APP_OrderActivity : AppCompatActivity() {

    private var selectedProduct: String? = null
    private val database = FirebaseDatabase.getInstance().getReference("orders")
    private val appOrderRef = FirebaseDatabase.getInstance()
        .getReference("orders/APPOrder/status")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apporder)

        val top = findViewById<TextView>(R.id.txt_timer_top)
        val bottom = findViewById<TextView>(R.id.txt_timer_bottom)

        // 🔹 앱이 실행되면 라즈베리 APP_BG ON
        appOrderRef.setValue(true)

        // === 상품 선택 버튼 ===
        val objViews = listOf<ImageView>(
            findViewById(R.id.obj1),
            findViewById(R.id.obj2),
            findViewById(R.id.obj3),
            findViewById(R.id.obj4),
            findViewById(R.id.obj5),
            findViewById(R.id.obj6)
        )

        objViews[0].setOnClickListener { selectProduct("P1") }
        objViews[1].setOnClickListener { selectProduct("P2") }
        objViews[2].setOnClickListener { selectProduct("P3") }
        objViews[3].setOnClickListener { selectProduct("P4") }
        objViews[4].setOnClickListener { selectProduct("P5") }
        objViews[5].setOnClickListener { selectProduct("P6") }

        // === 뒤로가기 버튼 & 구매내역 ===
        findViewById<ImageView>(R.id.btn_app).setOnClickListener { goToLog() }
        findViewById<ImageView>(R.id.btn_end).setOnClickListener {
            appOrderRef.setValue(false)
            sendOrderStatus("pending")
            goToMain()
        }

        // === 30초 타이머 ===
        object : android.os.CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000).toInt()
                top.text = sec.toString()
                bottom.text = sec.toString()
            }

            override fun onFinish() {
                appOrderRef.setValue(false)
                sendOrderStatus("pending")
                goToMain()
            }
        }.start()

        // ============================================
        // 🔥 Firebase 재고 적용 — SoldOut 이미지 반영
        // ============================================
        loadStockState(objViews)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 앱 전체가 닫힐 때 APP_BG OFF
        appOrderRef.setValue(false)
    }

    // ======================================================================
    // 🔷 Firebase에서 stock 1~6 읽어와서 SoldOut 이미지 적용
    // ======================================================================
    private fun loadStockState(objViews: List<ImageView>) {

        val stockRef = FirebaseDatabase.getInstance()
            .getReference("stock")   // ← 여기 중요!

        val soldImgs = listOf(
            R.mipmap.obj_img1_soldout,
            R.mipmap.obj_img2_soldout,
            R.mipmap.obj_img3_soldout,
            R.mipmap.obj_img4_soldout,
            R.mipmap.obj_img5_soldout,
            R.mipmap.obj_img6_soldout
        )

        val keys = listOf("A001", "A002", "A003", "A004", "A005", "A006")

        stockRef.get().addOnSuccessListener { snapshot ->

            for (i in 0 until 6) {
                val stockVal = snapshot.child(keys[i]).getValue(Int::class.java) ?: 0

                if (stockVal == 0) {
                    objViews[i].setImageResource(soldImgs[i])
                    objViews[i].isClickable = false
                    objViews[i].isEnabled = false
                }
            }
        }
    }


    // ======================================================================
    // 🔷 상품 선택 시 확인 팝업
    // ======================================================================
    private fun selectProduct(id: String) {
        selectedProduct = id
        showPurchasePopup()
    }

    private fun showPurchasePopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_paid_app)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.setDimAmount(0.7f)

        val btnYes = dialog.findViewById<ImageView>(R.id.btn_yes)
        val btnNo = dialog.findViewById<ImageView>(R.id.btn_no)
        val itemImg = dialog.findViewById<ImageView>(R.id.imageView17)

        when (selectedProduct) {
            "P1" -> itemImg.setImageResource(R.mipmap.p1_img)
            "P2" -> itemImg.setImageResource(R.mipmap.p2_img)
            "P3" -> itemImg.setImageResource(R.mipmap.p3_img)
            "P4" -> itemImg.setImageResource(R.mipmap.p4_img)
            "P5" -> itemImg.setImageResource(R.mipmap.p5_img)
            "P6" -> itemImg.setImageResource(R.mipmap.p6_img)
        }

        btnYes.setOnClickListener {
            dialog.dismiss()
            startAppPaymentProcess()
        }

        btnNo.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ======================================================================
    // 🔷 Firebase에 결제 완료 상태 보내기
    // ======================================================================
    private fun startAppPaymentProcess() {
        val productNum = selectedProduct?.substring(1)?.toIntOrNull() ?: return
        val orderId = "app_order_%03d".format(productNum)
        val productId = "A%03d".format(productNum)
        val ref = database.child(orderId)

        ref.updateChildren(
            mapOf(
                "status" to "paid",
                "product_id" to productId
            )
        )

        Handler().postDelayed({
            appOrderRef.setValue(false)
            goToMain()
        }, 1000)
    }

    private fun sendOrderStatus(status: String) {
        val productNum = selectedProduct?.substring(1)?.toIntOrNull() ?: 1
        val orderId = "app_order_%03d".format(productNum)
        val ref = database.child(orderId)
        ref.updateChildren(mapOf("status" to status))
    }

    // ======================================================================
    // 🔷 화면 이동
    // ======================================================================
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun goToLog() {
        val intent = Intent(this, PaidLogActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}
