package com.singhez.projemanag.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.singhez.projemanag.R
import com.singhez.projemanag.firebase.FirestoreClass
import com.singhez.projemanag.models.User
import com.singhez.projemanag.utils.Constants
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class MyProfileActivity : BaseActivity() {

    lateinit var user : User
    var changesMade = false
    private var profileImageURL : String = ""
    var imageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setUpActionBar()


        if (intent.hasExtra(Constants.USER_DETAILS_FROM_MAIN_TO_PROFILE)){
            user = intent.getParcelableExtra(Constants.USER_DETAILS_FROM_MAIN_TO_PROFILE)!!
        }

        showDataInUI()

        btn_update_my_profile.setOnClickListener {
            if (validateDetails()){
                if (imageChanged){
                    uploadImage()
                }else{
                    updateUserProfileData()
                }
            }
        }

        civ_user_my_profile.setOnClickListener {
            if (arePermissionGranted()){
                showImageOptionsDialog()
            }else{
                askForPermission()
            }
        }

    }


    fun setUpActionBar(){

        toolbar_my_profile.setNavigationIcon(R.drawable.ic_back_dark_primary)
        toolbar_my_profile.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK){
            if (requestCode == Constants.GALLERY){

                val contentUri = data!!.data!!
                val selectedImageBitmap = MediaStore.Images
                    .Media.getBitmap(this.contentResolver, contentUri)
                civ_user_my_profile.setImageBitmap(selectedImageBitmap)
                updateImageSize(contentUri)

                imageChanged = true
            }else if (requestCode == Constants.CAMERA){

                val thumbNail = data!!.extras!!.get("data") as Bitmap
                updateImageSize(getImageUri(this,thumbNail))
                civ_user_my_profile.setImageBitmap(thumbNail)

                imageChanged = true
            }
        }

    }

    fun getImageUri(context: Context, image: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            context.getContentResolver(),
            image,
            System.currentTimeMillis().toString(),
            null
        )
        return Uri.parse(path)
    }

    private fun updateImageSize(contentUri: Uri){
        lateinit var compressedImage : File
        val actualImage = File(getPath(contentUri)!!)

        lifecycleScope.launch {

            compressedImage = Compressor.compress(this@MyProfileActivity, actualImage)
            val uri = Uri.fromFile(compressedImage)
            Glide
                .with(this@MyProfileActivity)
                .load(uri)
                .centerCrop()
                .placeholder(R.drawable.ic_person_dark)
                .into(civ_user_my_profile)

        }

    }

    fun uploadImage(){

        showProgressDialog()

// Create a storage reference from our app
        val storageRef = Firebase.storage.reference

// Create a reference to "mountains.jpg"
        val imageRef = storageRef.child("USER_IMAGE" + user.id + System.currentTimeMillis() + "." + "jpeg")

        // Get the data from an ImageView as bytes
        civ_user_my_profile.isDrawingCacheEnabled = true
        civ_user_my_profile.buildDrawingCache()
        val bitmap = (civ_user_my_profile.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener { e ->
            // Handle unsuccessful uploads
            hideProgressDialog()
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()

        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...


            hideProgressDialog()
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                profileImageURL = uri.toString()
                updateUserProfileData()
            }


        }

    }

    fun updateUserProfileData(){

        val userHashMap = HashMap<String, Any>()

        if (et_name_my_profile.text.toString() != user.name){
            changesMade = true
            userHashMap[Constants.name] = et_name_my_profile.text.toString()
        }

        val mobile = et_mobile_my_profile.text.toString()

        if (mobile.isNotEmpty() && mobile != user.mobile.toString()){
            changesMade = true
            userHashMap[Constants.mobile] = et_mobile_my_profile.text.toString().toLong()
        }

        if (mobile.isEmpty() && user.mobile != 0L){
            changesMade = true
            userHashMap[Constants.mobile] = 0L
        }

        if (imageChanged){
            changesMade = true
            userHashMap[Constants.image] = profileImageURL
        }


        if (changesMade){
            showProgressDialog()
            FirestoreClass().updateUserDetails(this, userHashMap)
        }else{
            Toast.makeText(this, "Please make some changes", Toast.LENGTH_SHORT).show()
        }

    }

    private fun validateDetails() : Boolean {
        return when {
            et_name_my_profile.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar("Please enter your name")
                false
            }
//            et_mobile_my_profile.text.toString().isNotEmpty() && !Patterns.PHONE.matcher(et_mobile_my_profile.text
//                .toString().trim { it <= ' ' }).matches() -> {
//                showErrorSnackBar("Please enter a valid phone number")
//                false
//            }
            else -> {
                true
            }
        }
    }

    fun detailsUpdatedSuccessfully(){
        hideProgressDialog()
        Toast.makeText(this, "Details updated Successfully", Toast.LENGTH_SHORT).show()
        if(changesMade){
            setResult(RESULT_OK)
        }
    }

    private fun getReadableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun showDataInUI(){
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_person_dark)
            .into(civ_user_my_profile)

        et_name_my_profile.setText(user.name)
        et_email_my_profile.setText(user.email)

        if (user.mobile != 0L){
            et_mobile_my_profile.setText(user.mobile.toString())
        }

        //profileImageURL = user.image
    }

}