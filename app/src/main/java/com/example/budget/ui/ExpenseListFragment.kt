package com.example.budget.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budget.BudgetApplication
import com.example.budget.R
import com.example.budget.databinding.FragmentExpenseListBinding
import java.text.NumberFormat
import java.util.Locale

class ExpenseListFragment : Fragment() {
    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModel.ExpenseViewModelFactory((requireActivity().application as BudgetApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TransactionAdapter { transaction ->
            val bundle = Bundle().apply {
                putInt("transactionId", transaction.id)
                putString("title", "Editar Transacción")
            }
            findNavController().navigate(R.id.action_expenseListFragment_to_addExpenseFragment, bundle)
        }
        
        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
        }

        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())

        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            binding.tvTotalBalance.text = format.format(balance)
        }

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvTotalIncome.text = format.format(income ?: 0.0)
        }

        viewModel.totalExpenses.observe(viewLifecycleOwner) { expenses ->
            binding.tvTotalExpenses.text = format.format(expenses ?: 0.0)
        }

        binding.fabAddExpense.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("transactionId", -1)
                putString("title", "Añadir Transacción")
            }
            findNavController().navigate(R.id.action_expenseListFragment_to_addExpenseFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
