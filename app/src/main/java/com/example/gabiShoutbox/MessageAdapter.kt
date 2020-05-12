package com.example.gabiShoutbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_layout.view.*

class MessageAdapter(private val exampleList: Array<Message>) :
    RecyclerView.Adapter<MessageAdapter.ExampleViewHolder>() {
    class ExampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textContent: TextView = itemView.contentTextView;
        val textLogin: TextView = itemView.loginTextView;
        val textDate: TextView = itemView.dateTextView;
        val textTime: TextView = itemView.timeTextView;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_layout,
            parent, false
        )
        return ExampleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        val currentItem = exampleList[position]
        holder.textContent.text = currentItem.content
        holder.textLogin.text = currentItem.login
        holder.textDate.text = currentItem.date.toString().substring(0, 10)
        holder.textTime.text = currentItem.date.toString().substring(11, 19)
    }

    override fun getItemCount() = exampleList.size
}