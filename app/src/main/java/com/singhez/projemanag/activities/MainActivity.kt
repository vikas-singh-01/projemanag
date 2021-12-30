package com.singhez.projemanag.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singhez.projemanag.R
import com.singhez.projemanag.adapters.BoardsAdapter
import com.singhez.projemanag.databinding.ActivityMainBinding
import com.singhez.projemanag.databinding.AppBarMainBinding
import com.singhez.projemanag.databinding.MainContentBinding
import com.singhez.projemanag.firebase.FirestoreClass
import com.singhez.projemanag.models.Board
import com.singhez.projemanag.models.User
import com.singhez.projemanag.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : BaseActivity(){

    lateinit var user: User
    private lateinit var bindingMainActivity: ActivityMainBinding
    private lateinit var bindingMainContent : MainContentBinding
    private lateinit var bindingAppBarMainBinding: AppBarMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMainActivity = ActivityMainBinding.inflate(layoutInflater)
        bindingMainContent = MainContentBinding.inflate(layoutInflater)
        bindingAppBarMainBinding = AppBarMainBinding.inflate(layoutInflater)

        setContentView(bindingMainActivity.root)

        setupActionBar()



        bindingMainActivity.navViewMain.setNavigationItemSelectedListener { item ->
            navItemSelected(item)
        }

        showProgressDialog()

        FirestoreClass().loadUserDetails(this)

        FirestoreClass().getBoardsList(this)

        bindingAppBarMainBinding.fabMainActivity.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.name, user.name)
            startActivityForResult(intent, MAIN_TO_CREATE_BOARD_REQUEST_CODE)
        }

    }

    private fun setupActionBar(){
        bindingAppBarMainBinding.toolbarMainActivity.setNavigationIcon(R.drawable.ic_menu_dark_primary)
        bindingAppBarMainBinding.toolbarMainActivity.setNavigationOnClickListener {
            if (bindingMainActivity.drawerLayout.isDrawerOpen(GravityCompat.START)){
                bindingMainActivity.drawerLayout.closeDrawer(GravityCompat.START)
            }else{
                bindingMainActivity.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    override fun onBackPressed() {
        if(bindingMainActivity.drawerLayout.isDrawerOpen(GravityCompat.START)){
            bindingMainActivity.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }

    }

    private fun navItemSelected(item: MenuItem) : Boolean{
        when(item.itemId){
            R.id.nav_my_profile -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                intent.putExtra(Constants.USER_DETAILS_FROM_MAIN_TO_PROFILE,user)
                startActivityForResult(intent, MAIN_TO_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                showAlertDialog()
            }
        }
        bindingMainActivity.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user:User){
        this.user = user
        Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_person_white)
                .into(civ_user)

        tv_username.text = user.name
    }

    private fun showAlertDialog(){
        AlertDialog.Builder(this)
            .setTitle("Alert")
            .setMessage("Are you sure you want to Sign Out")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Yes")
            { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
                Firebase.auth.signOut()
                val intent = Intent(this,IntroActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No")
            { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAIN_TO_PROFILE_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                showProgressDialog()
                FirestoreClass().loadUserDetails(this)
                hideProgressDialog()
            }
        }else if (requestCode == MAIN_TO_CREATE_BOARD_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                showProgressDialog()
                FirestoreClass().getBoardsList(this)
            }
        }else if (requestCode == MAIN_TO_TASK_LIST_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                showProgressDialog()
                FirestoreClass().getBoardsList(this)
            }
        }

    }

    fun showBoards(list: ArrayList<Board>){

        hideProgressDialog()

        if (list.size > 0){
            tv_no_boards_available.visibility = View.GONE
            rv_boards.visibility = View.VISIBLE
        }else{
            tv_no_boards_available.visibility = View.VISIBLE
            rv_boards.visibility = View.GONE
        }

        val adapter = BoardsAdapter(this, list)
        rv_boards.layoutManager = LinearLayoutManager(this)

        rv_boards.adapter = adapter

        adapter.setOnClickListener(object : BoardsAdapter.OnClickListener{
            override fun onClick(position: Int, board: Board) {
                val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                intent.putExtra(Constants.BOARD_ID_FROM_MAIN_TO_TASK_LIST, board.document_ID)
                startActivityForResult(intent, MAIN_TO_TASK_LIST_REQUEST_CODE)
            }

        })

    }

    companion object{
        const val MAIN_TO_PROFILE_REQUEST_CODE = 1
        const val MAIN_TO_CREATE_BOARD_REQUEST_CODE = 2
        const val MAIN_TO_TASK_LIST_REQUEST_CODE = 3
    }

}