package com.singhez.projemanag.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.singhez.projemanag.R
import com.singhez.projemanag.models.Card
import kotlinx.android.synthetic.main.item_card.view.*
import kotlinx.android.synthetic.main.item_task.*

class CardListItemsAdapter(val context: Context,val list : ArrayList<Card>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.item_card,parent
                        ,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val card = list[position]
        val viewID = holder.itemView

        if (holder is MyViewHolder){
            viewID.tv_card_name_task_list.text = card.name

            if (card.labelColor.isNotEmpty()){
                viewID.view_color_label_card.setBackgroundColor(Color.parseColor(card.labelColor))
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    onClickListener!!.onClick(position,card)
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(cardPosition: Int, card: Card)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

}