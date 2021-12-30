package com.singhez.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.singhez.projemanag.R
import com.singhez.projemanag.adapters.TaskListItemsAdapter
import com.singhez.projemanag.firebase.FirestoreClass
import com.singhez.projemanag.models.Board
import com.singhez.projemanag.models.Card
import com.singhez.projemanag.models.Task
import com.singhez.projemanag.utils.Constants
import kotlinx.android.synthetic.main.activity_task_list.*

class TaskListActivity : BaseActivity() {

    lateinit var board: Board
    var boardID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if (intent.hasExtra(Constants.BOARD_ID_FROM_MAIN_TO_TASK_LIST)){
            boardID = intent.getStringExtra(Constants.BOARD_ID_FROM_MAIN_TO_TASK_LIST)!!
        }

        showProgressDialog()
        FirestoreClass().getBoardDetails(this,boardID)

    }

    fun cardDetails(taskPosition: Int, cardPosition:Int){
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.TASK_POSITION_FROM_TASK_TO_CARD_DETAILS,taskPosition)
        intent.putExtra(Constants.CARD_POSITION_FROM_TASK_TO_CARD_DETAILS,cardPosition)
        intent.putExtra(Constants.BOARD_FROM_TASK_TO_CARD_DETAILS,board)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CARD_DETAILS_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                showProgressDialog()
                FirestoreClass().getBoardDetails(this,boardID)
            }
        }else if(requestCode == MEMBER_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                showProgressDialog()
                FirestoreClass().getBoardDetails(this,boardID)
            }
        }

    }

    private fun setUpActionBar(){
        toolBar_task_list.setNavigationIcon(R.drawable.ic_back_dark_primary)
        toolBar_task_list.setNavigationOnClickListener {
            onBackPressed()
        }
        tv_board_name_task_list.text = board.name

        toolBar_task_list.setOnMenuItemClickListener { item: MenuItem ->
            onOptionsItemSelected(item) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.menu_action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARDS, board)
                startActivityForResult(intent, MEMBER_REQUEST_CODE)
            }
            R.id.menu_action_delete_board -> {
                alertDialogDeleteBoard()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun alertDialogDeleteBoard(){
        AlertDialog
                .Builder(this)
                .setTitle("Alert")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Are you sure you want to delete '${board.name}' ?")
                .setPositiveButton("Yes"){ dialogInterface, i ->
                    showProgressDialog()
                    FirestoreClass().deleteBoard(this, boardID)
                    dialogInterface.dismiss()
                }
                .setNegativeButton("No"){ dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                .setCancelable(false)
                .create()
                .show()
    }

    fun deleteBoardSuccess(){
        hideProgressDialog()
        setResult(RESULT_OK)
        finish()
    }

    fun showTasksList(board: Board){
        hideProgressDialog()
        this.board = board
        this.board.document_ID = boardID

        setUpActionBar()

        val addTaskList = Task("Add List")
        val list = board.task_list

        list.add(addTaskList)
        val adapter = TaskListItemsAdapter(this, list)
        rv_tasks.adapter = adapter
        rv_tasks.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog()
        FirestoreClass().getBoardDetails(this,boardID)
    }

    fun addTaskList(listName : String){
        val task = Task(listName, getCurrentUserID())

        board.task_list.add(0,task)
        board.task_list.removeAt(board.task_list.size-1)
        showProgressDialog()
        FirestoreClass().addUpdateTaskList(this,board)
    }

    fun renameTaskList(listName: String, position: Int, taskP: Task){
        val task = Task(listName, taskP.createdBy, taskP.cardsList)
        board.task_list[position] = task
        board.task_list.removeAt(board.task_list.size-1)

        showProgressDialog()
        FirestoreClass().addUpdateTaskList(this,board)
    }

    fun deleteTaskList(position: Int){
        board.task_list.removeAt(position)
        board.task_list.removeAt(board.task_list.size-1)

        showProgressDialog()
        FirestoreClass().addUpdateTaskList(this,board)
    }

    fun addCard(position: Int, cardName: String){
        val assignedTo = ArrayList<String>()
        assignedTo.add(getCurrentUserID())
        val card = Card(cardName, getCurrentUserID(), assignedTo)
        board.task_list[position].cardsList.add(card)
        board.task_list.removeAt(board.task_list.size-1)

        showProgressDialog()
        FirestoreClass().addUpdateTaskList(this,board)
    }

    companion object{
        const val CARD_DETAILS_REQUEST_CODE = 1
        const val MEMBER_REQUEST_CODE = 2
    }

}