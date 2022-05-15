package com.example.pomodoro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.pomodoro.databinding.PromodoroItemBinding

class PromodoroAdapter(private val listener: PromodoroListener): ListAdapter<Promodoro, PromodoroViewHolder>(itemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromodoroViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PromodoroItemBinding.inflate(layoutInflater, parent, false)
        return PromodoroViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: PromodoroViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {

        private val itemComparator = object : DiffUtil.ItemCallback<Promodoro>() {

            override fun areItemsTheSame(oldItem: Promodoro, newItem: Promodoro): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Promodoro, newItem: Promodoro): Boolean {
                return oldItem.timerMs == newItem.timerMs &&
                        oldItem.isStarted == newItem.isStarted
            }

            override fun getChangePayload(oldItem: Promodoro, newItem: Promodoro) = Any()
        }
    }
}