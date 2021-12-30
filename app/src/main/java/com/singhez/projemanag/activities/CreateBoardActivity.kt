package com.singhez.projemanag.activities

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
import com.singhez.projemanag.models.Board
import com.singhez.projemanag.utils.Constants
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File


class CreateBoardActivity : BaseActivity() {

    var imageChanged = false
    var profileImageURL = ""
    var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        if (intent.hasExtra(Constants.name)){
            name = intent.getStringExtra(Constants.name)!!
        }

        civ_board_create_board.setOnClickListener{
            if (arePermissionGranted()){
                showImageOptionsDialog()
            }else{
                askForPermission()
            }
        }

        setUpActionBar()

        btn_create_board.setOnClickListener {

            if (et_board_name.text.toString().trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            } else {
                if (imageChanged) {
                    uploadImage()
                } else {
                    createBoard()
                }
            }
        }

    }

    private fun setUpActionBar(){

        toolbar_create_board.setNavigationIcon(R.drawable.ic_back_dark_primary)
        toolbar_create_board.setNavigationOnClickListener {
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
                civ_board_create_board.setImageBitmap(selectedImageBitmap)
                //updateImageGallery(contentUri)

                imageChanged = true
            }else if (requestCode == Constants.CAMERA){

                val thumbNail = data!!.extras!!.get("data") as Bitmap
                civ_board_create_board.setImageBitmap(thumbNail)

                imageChanged = true
            }
        }

    }

    private fun updateImageGallery(contentUri: Uri){
        lateinit var compressedImage : File
        val actualImage = File(getPath(contentUri)!!)

        lifecycleScope.launch {

            compressedImage = Compressor.compress(this@CreateBoardActivity, actualImage)
            val uri = Uri.fromFile(compressedImage)
            Glide
                .with(this@CreateBoardActivity)
                .load(uri)
                .centerCrop()
                .placeholder(R.drawable.ic_board)
                .into(civ_board_create_board)

        }

    }

    private fun uploadImage(){

        showProgressDialog()

// Create a storage reference from our app
        val storageRef = Firebase.storage.reference

// Create a reference to "mountains.jpg"
        val imageRef = storageRef.child(
            "BOARD_IMAGE" +
                    FirestoreClass().getCurrentUserID() + System.currentTimeMillis() + "." + "jpeg"
        )

        // Get the data from an ImageView as bytes
        civ_board_create_board.isDrawingCacheEnabled = true
        civ_board_create_board.buildDrawingCache()
        val bitmap = (civ_board_create_board.drawable as BitmapDrawable).bitmap
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
                createBoard()
            }


        }

    }

    private fun createBoard(){
        showProgressDialog()
        val boardName = et_board_name.text.toString()
        val assignedTo = ArrayList<String>()
        assignedTo.add(getCurrentUserID())
        val board = Board(boardName, profileImageURL, name, assignedTo)

        FirestoreClass().createBoard(this, board)
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        Toast.makeText(this, "Board Created Successfully", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }


}