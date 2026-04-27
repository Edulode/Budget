package com.example.budget.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budget.data.Budget
import com.example.budget.databinding.ItemBudgetBinding
import java.text.NumberFormat
import java.util.*

class BudgetAdapter(
    private val spentMap: Map<String, Double>,
    private val onBudgetClick: (Budget) -> Unit
) : ListAdapter<Budget, BudgetAdapter.BudgetViewHolder>(BudgetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = getItem(position)
        holder.bind(budget, spentMap[budget.category] ?: 0.0)
        holder.itemView.setOnClickListener { onBudgetClick(budget) }
    }

    class BudgetViewHolder(private val binding: ItemBudgetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(budget: Budget, spent: Double) {
            binding.tvBudgetCategory.text = budget.category
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            binding.tvBudgetAmountInfo.text = "${format.format(spent)} de ${format.format(budget.amount)}"
            
            val progress = if (budget.amount > 0) (spent / budget.amount * 100).toInt() else 0
            binding.tvBudgetPercentage.text = "$progress%"
            binding.progressBudget.progress = progress.coerceAtMost(100)

            if (progress > 100) {
                binding.progressBudget.setIndicatorColor(Color.parseColor("#EF4444")) // Red
                binding.tvBudgetPercentage.setTextColor(Color.parseColor("#EF4444"))
            } else if (progress > 80) {
                binding.progressBudget.setIndicatorColor(Color.parseColor("#F59E0B")) // Amber
                binding.tvBudgetPercentage.setTextColor(Color.parseColor("#F59E0B"))
            } else {
                binding.progressBudget.setIndicatorColor(Color.parseColor("#22C55E")) // Green
                binding.tvBudgetPercentage.setTextColor(Color.parseColor("#22C55E"))
            }
        }
    }

    class BudgetDiffCallback : DiffUtil.ItemCallback<Budget>() {
        override fun areItemsTheSame(oldItem: Budget, newItem: Budget): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Budget, newItem: Budget): Boolean = oldItem == newItem
    }
}
