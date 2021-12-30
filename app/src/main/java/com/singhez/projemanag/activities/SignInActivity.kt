package com.singhez.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singhez.projemanag.R
import com.singhez.projemanag.firebase.FirestoreClass
import com.singhez.projemanag.models.User
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity() {

    lateinit var user : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        toolbar_sign_in.setNavigationIcon(R.drawable.ic_back_dark_primary)
        toolbar_sign_in.setNavigationOnClickListener {
            onBackPressed()
        }

        btn_sign_in_sign_in.setOnClickListener {
            if (validateDetails()){
                signInUser()
            }
        }

    }

    fun validateDetails() : Boolean{
        when{
            et_email_sign_in.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar("Please enter your email address")
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(et_email_sign_in.text
                    .toString().trim { it <= ' ' }).matches() -> {
                showErrorSnackBar("Please enter a valid email address")
                return false
            }
            et_password_sign_in.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar("please enter your password")
                return false
            }
            et_password_sign_in.text.toString().trim { it <= ' ' }.length < 8 -> {
                showErrorSnackBar("Invalid password")
                return false
            }
            else -> {
                return true
            }
        }
    }

    fun signInUser(){
        val email = et_email_sign_in.text.toString().trim { it <= ' ' }
        val password = et_password_sign_in.text.toString().trim { it <= ' ' }

        showProgressDialog()
        Firebase.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task->
            hideProgressDialog()
            if (task.isSuccessful){
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this, "Oops the email or password you entered is wrong",
                        Toast.LENGTH_LONG).show()
            }
        }

    }


}