package com.singhez.projemanag.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.singhez.projemanag.R
import com.singhez.projemanag.models.Board
import kotlinx.android.synthetic.main.item_board.view.*

open class BoardsAdapter(val context: Context, val list: ArrayList<Board>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_board,parent
            ,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val board = list[position]

        if (holder is MyViewHolder){

            Glide
                .with(context)
                .load(board.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board)
                .into(holder.itemView.civ_board_image_rv)

            holder.itemView.tv_board_name_rv.text = board.name
            holder.itemView.tv_created_by_rv.text = "Created by: " + board.created_by

            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    onClickListener!!.onClick(position,board)
                }
            }

        }

    }

    interface OnClickListener{
        fun onClick(position: Int, board: Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

}