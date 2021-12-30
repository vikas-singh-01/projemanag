package com.singhez.projemanag.adapters

import android.content.Context
import android.provider.SyncStateContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.auth.User
import com.singhez.projemanag.R
import com.singhez.projemanag.utils.Constants
import kotlinx.android.synthetic.main.item_member.view.*

class MemberListItemsAdpater(val context: Context,
                             val list: ArrayList<com.singhez.projemanag.models.User>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
         val view = LayoutInflater.from(context)
             .inflate(R.layout.item_member, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user: com.singhez.projemanag.models.User = list[position]

        if (holder is MyViewHolder){
            Glide
                .with(context)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_person_dark)
                .into(holder.itemView.civ_member_member)

            holder.itemView.tv_member_name_member.text = user.name
            holder.itemView.tv_member_email_member.text = user.email

            if (user.selected){
                holder.itemView.iv_member_selected.visibility = View.VISIBLE
            }else{
                holder.itemView.iv_member_selected.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    if (user.selected){
                        onClickListener!!.onClick(position,user,Constants.UN_SELECT)
                    }else{
                        onClickListener!!.onClick(position,user,Constants.SELECT)
                    }
                }
            }

        }
    }

    interface OnClickListener{
        fun onClick(position: Int, user: com.singhez.projemanag.models.User, action: String)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }


}