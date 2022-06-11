package com.example.fooddine

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    lateinit var etPhone: EditText
    lateinit var etCode: EditText
    lateinit var etCountryCode: EditText
    lateinit var btnCode: Button
    lateinit var btnVerify: Button
    lateinit var btnResend: Button
    lateinit var dialog: ProgressDialog
    lateinit var mAuth: FirebaseAuth;
    lateinit var verificationID: String
    lateinit var sPhone: String
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        initViews()
        mAuth = FirebaseAuth.getInstance()


        //callbacks
        callbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                verificationID = s
                dialog.dismiss()
                Toast.makeText(this@LoginActivity, "Check Your Phone for OTP!", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onCodeAutoRetrievalTimeOut(s: String) {
                super.onCodeAutoRetrievalTimeOut(s)
                btnResend.isEnabled = true
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                authenticateUser(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(
                    this@LoginActivity,
                    "Invalid Phone Number or OTP. Try again!" + e.message,
                    Toast.LENGTH_LONG
                ).show()
                etCountryCode.visibility = View.VISIBLE
                etCountryCode.setText("")
                etPhone.visibility = View.VISIBLE
                etPhone.setText("")
                btnCode.visibility = View.VISIBLE
                etCode.visibility = View.GONE
                btnVerify.visibility = View.GONE
                btnResend.visibility = View.GONE
                dialog.dismiss()
            }
        }

        btnCode.setOnClickListener{
            if (etCountryCode.text.toString().isEmpty()) {
                etCountryCode.error = "Enter Country Code"
                return@setOnClickListener
            }
            if (etPhone.text.toString().isEmpty()) {
                etPhone.error = "Enter Phone Number"
                return@setOnClickListener
            }
            sPhone =
                "+" + etCountryCode.text.toString().trim { it <= ' ' } + etPhone.text.toString()
                    .trim { it <= ' ' }
            dialog = ProgressDialog(this@LoginActivity)
            dialog.setMessage("Please Wait...")
            dialog.setCancelable(false)
            dialog.show()
            sendCode()
            etCountryCode.visibility = View.GONE
            etPhone.visibility = View.GONE
            btnCode.visibility = View.GONE
            etCode.visibility = View.VISIBLE
            btnVerify.visibility = View.VISIBLE
            btnResend.visibility = View.VISIBLE
            btnResend.isEnabled = false
        }

        btnVerify.setOnClickListener {
            if (etCode.text.toString().trim { it <= ' ' }.length < 6) {
                etCode.error = "Invalid OTP!"
                return@setOnClickListener
            }
            dialog.show()
            verifyCode(etCode.text.toString().trim { it <= ' ' })
        }

    }

    private fun initViews() {
        etPhone = findViewById(R.id.etPhone)
        etCode = findViewById(R.id.etCode)
        etCountryCode = findViewById(R.id.etCountryCode)
        btnCode = findViewById(R.id.btnCode)
        btnVerify = findViewById(R.id.btnVerify)
        btnResend = findViewById(R.id.btnResend)
    }

    fun sendCode() {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setActivity(this@LoginActivity)
            .setCallbacks(callbacks)
            .setPhoneNumber(sPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(otp: String?) {
        val credential = PhoneAuthProvider.getCredential(verificationID, otp!!)
        authenticateUser(credential)
    }

    // Authenticate sign in
    fun authenticateUser(credential: PhoneAuthCredential?) {
        mAuth.signInWithCredential(credential!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Handler().postDelayed({
                        dialog.dismiss()
                        // Move to home screen
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }, 2000)
                } else {
                    dialog.dismiss()
                    Toast.makeText(
                        this@LoginActivity,
                        "Failed: " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}