package com.wellness.vet.app.activities.common

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wellness.vet.app.PaymentActivity
import com.wellness.vet.app.R


class MainTestAuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_test_auth)

        val i = Intent(this@MainTestAuthActivity, PaymentActivity::class.java)
        //startActivity(new Intent(MainActivity.this, PaymentActivity.class));
        //startActivity(new Intent(MainActivity.this, PaymentActivity.class));
//        i.putExtra("price", price.getText().toString())

        startActivityForResult(i, 0)


//        val auth: FirebaseAuth = FirebaseAuth.getInstance()
//        val user = FirebaseAuth.getInstance().currentUser
//        if (user != null) {
////            startActivity(Intent(this@MainTestAuthActivity, UserChatActivity::class.java))
//            startActivity(Intent(this@MainTestAuthActivity,UserDashBoardActivity::class.java))
//        } else {
////            doctor@abc.com
//            auth.signInWithEmailAndPassword("user@abc.com", "112233")
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        val user = auth.currentUser
//                        startActivity(Intent(this@MainTestAuthActivity, UserDashBoardActivity::class.java))
//                    } else {
//
//                    }
//                }
//        }
    }

    @Deprecated("Deprecated in Java")
    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check that it is the SecondActivity with an OK result
        if (requestCode == 0 && resultCode == RESULT_OK) {
            // Get String data from Intent
            val ResponseCode = data?.getStringExtra("pp_ResponseCode")
            println("DateFn: ResponseCode:$ResponseCode")
            if (ResponseCode == "000") {
                Toast.makeText(applicationContext, "Payment Success", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Payment Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}