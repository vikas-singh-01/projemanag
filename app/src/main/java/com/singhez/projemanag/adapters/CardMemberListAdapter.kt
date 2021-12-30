package com.singhez.projemanag.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.singhez.projemanag.R
import com.singhez.projemanag.models.SelectedMembers
import kotlinx.android.synthetic.main.item_selected_member_card_details.view.*

class CardMemberListAdapter(val context: Context, val list: ArrayList<SelectedMembers>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_selected_member_card_details,
                parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val selectedMember = list[position]

        if (holder is MyViewHolder){
            if (position == list.size -1){
                holder.itemView.ll_selected_member.visibility = View.GONE
                holder.itemView.ll_select_member.visibility = View.VISIBLE

                holder.itemView.setOnClickListener {
                    onClickListener!!.onClick()
                }
            }else{
                holder.itemView.ll_selected_member.visibility = View.VISIBLE
                holder.itemView.ll_select_member.visibility = View.GONE

                holder.itemView.tv_selected_member_name.text = selectedMember.name

                Glide
                    .with(context)
                    .load(selectedMember.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_person_dark)
                    .into(holder.itemView.civ_selected_member_image)

            }
        }

    }

    interface OnClickListener{
        fun onClick()
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }
}