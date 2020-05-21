package com.example.gabiShoutbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_layout.view.*

class MessageAdapter(
    private val myList: ArrayList<Message>,
    var clickListener: OnItemClickListener
) : RecyclerView.Adapter<MessageAdapter.ExampleViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_layout,
            parent, false
        )

        return ExampleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        holder.initialize(myList[position], clickListener)
    }

    override fun getItemCount() = myList.size

    class ExampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textText: TextView = itemView.contentTextView
        var textLog: TextView = itemView.loginTextView
        var textDate: TextView = itemView.dateTextView
        var textHour: TextView = itemView.timeTextView
        fun initialize(item: Message, action: OnItemClickListener) {
            textText.text = item.content
            textLog.text = item.login
            textDate.text = item.date.toString().subSequence(0, 10)
            textHour.text = item.date.toString().subSequence(11, 19)

            itemView.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: Message, position: Int)
    }


    fun getItem(position: Int): Message {
        return myList[position]
    }

    fun removeAt(position: Int) {
        myList.removeAt(position)
        notifyItemRemoved(position)
    }
}