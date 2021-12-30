package com.singhez.projemanag.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.singhez.projemanag.R
import com.singhez.projemanag.utils.Constants

open class BaseActivity : AppCompatActivity() {

    var doubleBackToExitPressedOnce = false
    lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

    }

    fun showProgressDialog(){
        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.dialog_progress)
        progressDialog.show()
    }

    fun hideProgressDialog(){
        progressDialog.dismiss()
    }

    fun getCurrentUserID() : String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubleBackToExit(){
        if (doubleBackToExitPressedOnce){
            super.onBackPressed()
        }else{
            doubleBackToExitPressedOnce = true
            Toast.makeText(this,"Please press back again to exit",
                Toast.LENGTH_SHORT).show()
            Handler().postDelayed({doubleBackToExitPressedOnce = false},2000)
        }
    }

    fun showErrorSnackBar(message : String){
        val snackbar = Snackbar.make(findViewById(android.R.id.content),
                message,Snackbar.LENGTH_LONG)
        snackbar.setTextColor(getColor(R.color.white))
        snackbar.setBackgroundTint(Color.RED)
        snackbar.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permissions Granted", Toast.LENGTH_SHORT).show()
            }else{
                try {
                    val intent =  Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
        }

    }

    fun askForPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ),
            Constants.READ_STORAGE_PERMISSION_CODE
        )
    }

    fun arePermissionGranted() : Boolean{
        val storage = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val camera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        return storage && camera
    }

    fun showImageOptionsDialog(){
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Please select an action")
        val pictureDialogItems = arrayOf(
            "Select a photo from gallery",
            "Capture a photo from camera"
        )
        pictureDialog.setItems(pictureDialogItems){ dialog, which ->
            when(which){
                0 -> choosePhotoFromGallery()
                1 -> capturePhotoFromCamera()
            }
        }

        pictureDialog.show()
    }

    fun choosePhotoFromGallery(){

        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, Constants.GALLERY)

    }

    fun capturePhotoFromCamera(){

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, Constants.CAMERA)

    }

    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection,
            null, null, null) ?: return null
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s = cursor.getString(column_index)
        cursor.close()
        return s
    }

}