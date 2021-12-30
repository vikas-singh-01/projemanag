package com.singhez.projemanag.activities

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.singhez.projemanag.R
import com.singhez.projemanag.adapters.CardMemberListAdapter
import com.singhez.projemanag.adapters.LabelColorItemsAdapter
import com.singhez.projemanag.adapters.MemberListItemsAdpater
import com.singhez.projemanag.firebase.FirestoreClass
import com.singhez.projemanag.models.Board
import com.singhez.projemanag.models.Card
import com.singhez.projemanag.models.SelectedMembers
import com.singhez.projemanag.models.User
import com.singhez.projemanag.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_card_details.view.*
import kotlinx.android.synthetic.main.dialog_card_members.view.*
import kotlinx.android.synthetic.main.dialog_label_color_list.*
import kotlinx.android.synthetic.main.dialog_label_color_list.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CardDetailsActivity : BaseActivity() {

    var changesMade = false
    var cardPosition: Int = 0
    lateinit var board: Board
    var taskPosition: Int = 0
    lateinit var card: Card
    var selectedColor : String = ""
    var selectedDate : String = ""
    var allMembers = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntents()
        showProgressDialog()
        FirestoreClass().getMembersListDetails(this,board.assigned_to)
        setUpActionBar()
        showDataInUI()

        tv_select_color_card_details.setOnClickListener {
            showColorChooserDialog()
        }

        tv_select_due_date_card_details.setOnClickListener {
            showDatePickerDialog()
        }

        tv_select_members_card_details.setOnClickListener {
            showMembersDialog()
        }

        btn_update_card_details.setOnClickListener {
            if(validateDetails()){
                updateCardDetails()
            }
        }

    }

    fun getAllMemberList(list : ArrayList<User>){
        hideProgressDialog()
        allMembers = list
        setUpSelectedMembersList()
    }

    fun getIntents(){
        board = intent.getParcelableExtra(Constants.BOARD_FROM_TASK_TO_CARD_DETAILS)!!
        cardPosition = intent.getIntExtra(Constants.CARD_POSITION_FROM_TASK_TO_CARD_DETAILS, 0)
        taskPosition = intent.getIntExtra(Constants.TASK_POSITION_FROM_TASK_TO_CARD_DETAILS, 0)
        card = board.task_list[taskPosition].cardsList[cardPosition]
    }

    fun setUpActionBar(){
        toolbarCardDetailsActivity.setNavigationIcon(R.drawable.ic_back_dark_primary)
        toolbarCardDetailsActivity.setNavigationOnClickListener {
            onBackPressed()
        }
        tv_card_name_card_details.text = card.name

        toolbarCardDetailsActivity.inflateMenu(R.menu.menu_delete_card)
        toolbarCardDetailsActivity.setOnMenuItemClickListener { item ->
            onOptionsItemSelected(item)

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.menu_action_delete_card -> {
                alertDialogDeleteCard()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun alertDialogDeleteCard(){
        AlertDialog
            .Builder(this)
            .setTitle("Alert")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage("Are you sure you want to delete '${card.name}' ?")
            .setPositiveButton("Yes"){ dialogInterface, i ->
                showProgressDialog()
                board.task_list[taskPosition].cardsList.removeAt(cardPosition)
                board.task_list.removeAt(board.task_list.size-1)
                FirestoreClass().addUpdateTaskList(this,board)
                setResult(RESULT_OK)
                finish()
                dialogInterface.dismiss()
            }
            .setNegativeButton("No"){ dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    fun showDataInUI(){
        et_card_name_card_details.setText(card.name)
        if (card.labelColor.isNotEmpty()){
            tv_select_color_card_details.text = ""
            tv_select_color_card_details.setBackgroundColor(Color.parseColor(card.labelColor))
        }
        if (card.dueDate.isNotEmpty()){
            tv_select_due_date_card_details.text = card.dueDate
        }
    }

    private fun colorsList() : ArrayList<String>{
        val colorList  = ArrayList<String>()
        colorList.add("#43C86F")
        colorList.add("#0C90F1")
        colorList.add("#F72400")
        colorList.add("#7A8089")
        colorList.add("#D57C1D")
        colorList.add("#770000")
        colorList.add("#0022F8")
        return colorList
    }

    fun showColorChooserDialog(){
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(
            R.layout.dialog_label_color_list, null, false
        )

        dialog.setContentView(view)
        view.rv_label_color.layoutManager = LinearLayoutManager(this)
        val adapter = LabelColorItemsAdapter(this, colorsList(), card.labelColor)
        view.rv_label_color.adapter = adapter

        dialog.show()

        adapter.setOnClickListener(
            object : LabelColorItemsAdapter.OnClickListener{
                override fun onClick(position: Int, color: String) {
                    selectedColor = color
                    dialog.dismiss()
                    tv_select_color_card_details.text = ""
                    tv_select_color_card_details.setBackgroundColor(Color.parseColor(selectedColor))
                }

            }
        )
    }

    fun showMembersDialog(){
        val cardMembers = board.task_list[taskPosition].cardsList[cardPosition].assignedTo

        for (member in allMembers){
            for (cardMember in cardMembers){
                if (member.id == cardMember){
                    member.selected = true
                }
            }
        }

        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(
            R.layout.dialog_card_members, null, false
        )

        dialog.setContentView(view)
        view.rv_card_members_card_details.layoutManager = LinearLayoutManager(this)
        val adapter = MemberListItemsAdpater(this,allMembers)
        view.rv_card_members_card_details.adapter = adapter

        dialog.show()

        adapter.setOnClickListener(
            object : MemberListItemsAdpater.OnClickListener{
                override fun onClick(position: Int, user: User, action: String) {
                    if (action == Constants.SELECT){
                        if (!cardMembers.contains(user.id)){
                            cardMembers.add(user.id)
                        }
                        changesMade = true
                        allMembers[position].selected = true
                    }else if(action == Constants.UN_SELECT){
                        if (cardMembers.contains(user.id)){
                            cardMembers.remove(user.id)
                        }
//                        for (member in allMembers){
//                            if (member.id == user.id){
//                                member.selected = false
//                            }
//                        }
                        changesMade = true
                        allMembers[position].selected = false
                    }
                    dialog.dismiss()
                    setUpSelectedMembersList()
                }
            }
        )

    }

    fun showDatePickerDialog(){
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener{
                view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)

            val format = "EEE, d MMM yyyy"
            val sdf= SimpleDateFormat(format,Locale.getDefault())
            selectedDate = sdf.format(cal.time).toString()
            tv_select_due_date_card_details.setText(selectedDate)
        }

        DatePickerDialog(this,
            R.style.MyDatePickerStyle,
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()

    }

    fun validateDetails(): Boolean{
        val enteredName = et_card_name_card_details.text.toString()
        if (enteredName.isEmpty()){
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            return false
        }
        when{
            card.name != enteredName -> {
                changesMade = true
            }
            card.labelColor != selectedColor && selectedColor.isNotEmpty() -> {
                changesMade = true
            }
            card.dueDate != selectedDate && selectedDate.isNotEmpty() -> {
                changesMade = true
            }
        }
        if (!changesMade){
            Toast.makeText(this, "Please make some changes", Toast.LENGTH_SHORT).show()
        }
        return changesMade
    }

    fun updateCardDetails(){
        val name = et_card_name_card_details.text.toString()
        if (selectedColor.isEmpty()){
            selectedColor = card.labelColor
        }
        if (selectedDate.isEmpty()){
            selectedDate = card.dueDate
        }
        val assignedTo = board.task_list[taskPosition].cardsList[cardPosition].assignedTo
        val card = Card(name,getCurrentUserID(),assignedTo,selectedColor,selectedDate)
        this.card = card
        board.task_list[taskPosition].cardsList[cardPosition] = card
        board.task_list.removeAt(board.task_list.size -1)

        showProgressDialog()
        FirestoreClass().addUpdateTaskList(this,board)
    }

    fun updateCardSuccess(){
        hideProgressDialog()
        setResult(RESULT_OK)
        Toast.makeText(this, "Done Successfully", Toast.LENGTH_SHORT).show()
    }

    fun setUpSelectedMembersList(){

        val cardMembers = board.task_list[taskPosition].cardsList[cardPosition].assignedTo

        val selectedMembers = ArrayList<SelectedMembers>()

        for(i in allMembers){
            for (j in cardMembers){
                if (i.id == j){
                    val selectedMember = SelectedMembers(i.id,i.image,i.name)
                    selectedMembers.add(selectedMember)
                }
            }
        }

        if (selectedMembers.size > 0){

            selectedMembers.add(SelectedMembers("","",""))

            tv_select_members_card_details.visibility = View.GONE
            rv_members_card_details.visibility = View.VISIBLE

            val adapter = CardMemberListAdapter(this,selectedMembers)
            rv_members_card_details.layoutManager = GridLayoutManager(this,4)
            rv_members_card_details.adapter = adapter

            adapter.setOnClickListener(
                object : CardMemberListAdapter.OnClickListener{
                    override fun onClick() {
                        showMembersDialog()
                    }

                }
            )

        }else{

            tv_select_members_card_details.visibility = View.VISIBLE
            rv_members_card_details.visibility = View.GONE
        }

    }

}