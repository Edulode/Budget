package com.example.budget.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budget.BudgetApplication
import com.example.budget.R
import com.example.budget.data.Budget
import com.example.budget.data.TransactionType
import com.example.budget.databinding.FragmentBudgetBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import java.text.NumberFormat
import java.util.*

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModel.ExpenseViewModelFactory((requireActivity().application as BudgetApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cal = Calendar.getInstance()
        val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val year = cal.get(Calendar.YEAR)
        binding.tvMonthYear.text = "$monthName $year"

        viewModel.expenseCategorySummaries.observe(viewLifecycleOwner) { summaries ->
            val spentMap = summaries.associate { it.category to it.amount }
            setupRecyclerView(spentMap)
            updateTotalProgress(spentMap)
        }

        binding.fabSetBudget.setOnClickListener {
            showBudgetDialog(null)
        }
    }

    private fun setupRecyclerView(spentMap: Map<String, Double>) {
        val adapter = BudgetAdapter(spentMap) { budget ->
            showBudgetDialog(budget)
        }
        binding.rvBudgets.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        viewModel.getBudgetsForCurrentMonth().observe(viewLifecycleOwner) { budgets ->
            adapter.submitList(budgets)
        }
    }

    private fun updateTotalProgress(spentMap: Map<String, Double>) {
        viewModel.getBudgetsForCurrentMonth().observe(viewLifecycleOwner) { budgets ->
            val totalBudget = budgets.sumOf { it.amount }
            val totalSpent = budgets.sumOf { spentMap[it.category] ?: 0.0 }
            
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            binding.tvSpentAmount.text = "${format.format(totalSpent)} gastados"
            binding.tvTotalBudgetLimit.text = "de ${format.format(totalBudget)}"
            
            val progress = if (totalBudget > 0) (totalSpent / totalBudget * 100).toInt() else 0
            binding.progressTotalBudget.progress = progress.coerceAtMost(100)
        }
    }

    private fun showBudgetDialog(budget: Budget?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_budget, null)
        val actvCategory = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.actvBudgetCategory)
        val etAmount = dialogView.findViewById<EditText>(R.id.etBudgetAmount)

        etAmount.addTextChangedListener(MoneyTextWatcher(etAmount))

        viewModel.getCategoriesByType(TransactionType.EXPENSE).observe(viewLifecycleOwner) { categories ->
            val names = categories.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
            actvCategory.setAdapter(adapter)
        }

        if (budget != null) {
            actvCategory.setText(budget.category, false)
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            etAmount.setText(format.format(budget.amount))
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (budget == null) "Nuevo Límite" else "Editar Límite")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val category = actvCategory.text.toString()
                val amountStr = etAmount.text.toString().replace("[$,]".toRegex(), "")
                if (category.isNotEmpty() && amountStr.isNotEmpty()) {
                    val cal = Calendar.getInstance()
                    val newBudget = Budget(
                        id = budget?.id ?: 0,
                        category = category,
                        amount = amountStr.toDouble(),
                        month = cal.get(Calendar.MONTH) + 1,
                        year = cal.get(Calendar.YEAR)
                    )
                    if (budget == null) viewModel.insertBudget(newBudget)
                    else viewModel.updateBudget(newBudget)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
