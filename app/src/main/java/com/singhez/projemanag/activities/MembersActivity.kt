package com.singhez.projemanag.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.singhez.projemanag.R
import com.singhez.projemanag.adapters.MemberListItemsAdpater
import com.singhez.projemanag.firebase.FirestoreClass
import com.singhez.projemanag.models.Board
import com.singhez.projemanag.models.User
import com.singhez.projemanag.utils.Constants
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.dialog_search_member.*

class MembersActivity : BaseActivity() {

    lateinit var board: Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        board = intent.getParcelableExtra(Constants.BOARDS)!!
        setUpActionBar()

        showProgressDialog()
        FirestoreClass().getMembersListDetails(this, board.assigned_to)

    }

    fun setUpActionBar(){
        toolbar_members_activity.setNavigationIcon(R.drawable.ic_back_dark_primary)
        toolbar_members_activity.setNavigationOnClickListener {
            onBackPressed()
        }

        toolbar_members_activity.inflateMenu(R.menu.menu_add_member)
        toolbar_members_activity.setOnMenuItemClickListener { item ->
            onOptionsItemSelected(item)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.menu_action_add_member -> {
                addMemberDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun addMemberDialog(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.setCancelable(false)
        dialog.tv_add_member_member.setOnClickListener {
            val email = dialog.et_email_add_member.text.toString()
            if(email.isEmpty()){
                Toast.makeText(this,
                    "Please enter email of member", Toast.LENGTH_SHORT).show()
            }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this,
                    "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            }else{
                showProgressDialog()
                FirestoreClass().getMemberDetails(this, email)
                dialog.dismiss()
            }
        }
        dialog.tv_cancel_member.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun memberFound(user: User){
        val id = user.id
        val assignedMembers : ArrayList<String> = board.assigned_to
        assignedMembers.add(id)
        val hashMap = HashMap<String, Any>()
        hashMap[Constants.ASSIGNED_TO] = assignedMembers
        FirestoreClass().updateBoardDetails(this,hashMap,board.document_ID)
    }

    fun memberUpdatedSuccessfully(){
        hideProgressDialog()
        showProgressDialog()
        setResult(RESULT_OK)
        FirestoreClass().getMembersListDetails(this, board.assigned_to)
    }

    fun showMembers(memberList : ArrayList<User>){
        hideProgressDialog()
        val adapter = MemberListItemsAdpater(this, memberList)

        rv_members_members_activity.adapter = adapter
        rv_members_members_activity.layoutManager = LinearLayoutManager(this)
    }


}