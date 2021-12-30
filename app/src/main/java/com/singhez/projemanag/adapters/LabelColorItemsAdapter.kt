package com.singhez.projemanag.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.singhez.projemanag.R
import kotlinx.android.synthetic.main.item_label_color.view.*

class LabelColorItemsAdapter(val context: Context, val list: ArrayList<String>,
                             val selectedColor : String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_label_color, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val color = list[position]
        if (holder is MyViewHolder){
            holder.itemView.view_label_color.setBackgroundColor(Color.parseColor(color))

            if (selectedColor.isNotEmpty() && color == selectedColor){
                holder.itemView.iv_label_color_selected.visibility = View.VISIBLE
            }else{
                holder.itemView.iv_label_color_selected.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    onClickListener!!.onClick(position,color)
                }
            }

        }
    }

    interface OnClickListener{
        fun onClick(position: Int, color: String)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }
}