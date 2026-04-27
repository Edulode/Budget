package com.example.budget.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budget.data.CategorySummary
import com.example.budget.databinding.ItemExpenseBinding // Reutilizamos el diseño por simplicidad o crea uno nuevo
import java.text.NumberFormat
import java.util.Locale

class StatisticsAdapter : ListAdapter<CategorySummary, StatisticsAdapter.StatsViewHolder>(StatsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StatsViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(summary: CategorySummary) {
            binding.tvTitle.text = summary.category
            binding.tvCategory.text = "Total gastado"
            binding.tvAmount.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(summary.amount)
        }
    }

    class StatsDiffCallback : DiffUtil.ItemCallback<CategorySummary>() {
        override fun areItemsTheSame(oldItem: CategorySummary, newItem: CategorySummary): Boolean = 
            oldItem.category == newItem.category
        override fun areContentsTheSame(oldItem: CategorySummary, newItem: CategorySummary): Boolean = 
            oldItem == newItem
    }
}
