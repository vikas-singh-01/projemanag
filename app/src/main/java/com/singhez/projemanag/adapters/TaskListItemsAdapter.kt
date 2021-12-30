package com.singhez.projemanag.adapters

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singhez.projemanag.R
import com.singhez.projemanag.activities.IntroActivity
import com.singhez.projemanag.activities.TaskListActivity
import com.singhez.projemanag.models.Card
import com.singhez.projemanag.models.Task
import kotlinx.android.synthetic.main.item_task.view.*

class TaskListItemsAdapter(val context: Context, val list: ArrayList<Task>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.item_task, parent,
                        false)
        val layoutParams = LinearLayout
                .LayoutParams((parent.width*0.7).toInt(),
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        view.layoutParams = layoutParams
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val task = list[position]

        if (holder is MyViewHolder){
            val viewID = holder.itemView

            if (position == list.size -1){
                viewID.tv_add_list_task_list.visibility = View.VISIBLE
            }else{
                viewID.ll_list_name.visibility = View.VISIBLE
                viewID.tv_add_list_task_list.visibility = View.GONE
                viewID.ll_cards_rv.visibility = View.VISIBLE
                viewID.tv_add_card_task_list.visibility = View.VISIBLE
                viewID.view_underline_task_list_name.visibility = View.VISIBLE
            }

            viewID.tv_list_name.text = task.name

            manageAddingTaskList(holder)

            manageEditingCard(holder, position, task)

            manageDeletingList(holder,task.name, position)

            manageAddingCards(holder,position)

            showCards(holder, task, position)
        }
    }

    fun manageAddingTaskList(holder: RecyclerView.ViewHolder){
        val viewID = holder.itemView

        viewID.tv_add_list_task_list.setOnClickListener {
            viewID.tv_add_list_task_list.visibility = View.GONE
            viewID.cv_add_list.visibility = View.VISIBLE
        }

        viewID.ib_close_add_list_name.setOnClickListener {
            viewID.tv_add_list_task_list.visibility = View.VISIBLE
            viewID.cv_add_list.visibility = View.GONE
        }

        viewID.ib_done_add_list_name.setOnClickListener {
            val listName = viewID.et_add_list_name.text.toString()

            if (listName.isEmpty()){
                Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
            }else{
                if (context is TaskListActivity){
                    context.addTaskList(listName)
                }
            }
        }
    }

    fun manageEditingCard(holder: RecyclerView.ViewHolder, position: Int, task: Task){
        val viewID = holder.itemView

        viewID.ib_edit_list_name.setOnClickListener {
            viewID.ll_list_name.visibility = View.GONE
            viewID.cv_edit_list_name.visibility = View.VISIBLE

            viewID.et_edit_list_name.setText(task.name)
        }

        viewID.ib_close_edit_list_name.setOnClickListener {
            viewID.ll_list_name.visibility = View.VISIBLE
            viewID.cv_edit_list_name.visibility = View.GONE
        }

        viewID.ib_done_edit_list_name.setOnClickListener {
            val listName = viewID.et_edit_list_name.text.toString()
            if (listName.isEmpty()){
                Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
            }else if(listName == task.name){
                Toast.makeText(context, "Please make some changes", Toast.LENGTH_SHORT).show()
            }else{
                if (context is TaskListActivity){
                    context.renameTaskList(listName, position, task)
                }
            }
        }

    }

    fun manageDeletingList(holder: RecyclerView.ViewHolder, name : String, position: Int){
        val viewID = holder.itemView

        viewID.ib_delete_list_name.setOnClickListener {
            AlertDialog.Builder(context)
                    .setTitle("Alert")
                    .setMessage("Are you sure you want to delete '$name' ?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes")
                    { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                        if (context is TaskListActivity){
                            context.deleteTaskList(position)
                        }
                    }
                    .setNegativeButton("No")
                    { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
        }
    }

    fun manageAddingCards(holder: RecyclerView.ViewHolder, position: Int){
        val viewID = holder.itemView

        viewID.tv_add_card_task_list.setOnClickListener {
            viewID.tv_add_card_task_list.visibility = View.GONE
            viewID.cv_add_card.visibility = View.VISIBLE
        }

        viewID.ib_close_add_card_name.setOnClickListener {
            viewID.tv_add_card_task_list.visibility = View.VISIBLE
            viewID.cv_add_card.visibility = View.GONE
        }

        viewID.ib_done_add_card_name.setOnClickListener {
            val cardName = viewID.et_add_card_name.text.toString()

            if (cardName.isEmpty()){
                Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
            }else{
                if (context is TaskListActivity){
                    context.addCard(position,cardName)
                }
            }
        }
    }

    fun showCards(holder: RecyclerView.ViewHolder, task: Task, position: Int){

        val viewID = holder.itemView
        val cards = task.cardsList
        val adapter = CardListItemsAdapter(context, cards)
        viewID.rv_cards_list.adapter = adapter
        viewID.rv_cards_list.layoutManager = LinearLayoutManager(context)

        adapter.setOnClickListener(
            object : CardListItemsAdapter.OnClickListener{
                override fun onClick(cardPosition: Int, card: Card) {
                    if (context is TaskListActivity){
                        context.cardDetails(position,cardPosition)
                    }
                }

            }
        )

    }

    override fun getItemCount(): Int {
        return list.size
    }


}