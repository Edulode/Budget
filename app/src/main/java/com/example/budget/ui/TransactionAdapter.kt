package com.example.budget.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budget.R
import com.example.budget.data.Transaction
import com.example.budget.data.TransactionType
import com.example.budget.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(private val onTransactionClick: (Transaction) -> Unit) : 
    ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
        holder.itemView.setOnClickListener { onTransactionClick(transaction) }
    }

    class TransactionViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun bind(transaction: Transaction) {
            binding.tvTitle.text = transaction.title
            binding.tvCategory.text = "${transaction.category} • ${dateFormat.format(Date(transaction.date))}"
            
            val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
            
            if (transaction.type == TransactionType.INCOME) {
                binding.tvAmount.text = "+ ${format.format(transaction.amount)}"
                binding.tvAmount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.income_indicator))
                binding.iconContainer.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.income_light))
                binding.ivTypeIcon.setImageResource(android.R.drawable.ic_input_add)
                binding.ivTypeIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.income_indicator))
            } else {
                binding.tvAmount.text = "- ${format.format(transaction.amount)}"
                binding.tvAmount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.expense_indicator))
                binding.iconContainer.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.expense_light))
                binding.ivTypeIcon.setImageResource(android.R.drawable.ic_delete)
                binding.ivTypeIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.expense_indicator))
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem == newItem
    }
}
