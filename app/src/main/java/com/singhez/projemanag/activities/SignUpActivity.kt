package com.singhez.projemanag.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singhez.projemanag.R
import com.singhez.projemanag.firebase.FirestoreClass
import com.singhez.projemanag.models.User
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {

    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        toolbar_sign_up.setNavigationIcon(R.drawable.ic_back_dark_primary)
        toolbar_sign_up.setNavigationOnClickListener {
            onBackPressed()
        }

        btn_sign_up_sign_up.setOnClickListener {
            if (validateDetails()){
                registerUser()
            }
        }

    }

    fun validateDetails() : Boolean{
        when{
            et_name_sign_up.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar("Please enter your name")
                return false
            }
            et_email_sign_up.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar("Please enter your email address")
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(et_email_sign_up.text
                    .toString().trim { it <= ' ' }).matches() -> {
                showErrorSnackBar("Please enter a valid email address")
                return false
            }
            et_password_sign_up.text.toString().trim { it <= ' ' }.length < 8 -> {
                showErrorSnackBar("password must contain atleast 8 characters")
                return false
            }
            else -> {
                return true
            }
        }
    }

    fun registerUser(){
        val name = et_name_sign_up.text.toString()
        val email = et_email_sign_up.text.toString()
        val password = et_password_sign_up.text.toString()

        showProgressDialog()

        Firebase.auth.createUserWithEmailAndPassword(email,
                password).addOnCompleteListener {
            task ->
            if (task.isSuccessful){
                val userinfo = User(getCurrentUserID(),name,email, password)
                FirestoreClass().registerUser(this,userinfo)
                user = userinfo
            }else{
                hideProgressDialog()
                Toast.makeText(this, task.exception!!.message,
                        Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun userRegisteredSuccessfully(){
        hideProgressDialog()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}