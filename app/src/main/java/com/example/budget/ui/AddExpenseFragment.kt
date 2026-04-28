package com.example.budget.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.budget.BudgetApplication
import com.example.budget.R
import com.example.budget.data.Transaction
import com.example.budget.data.TransactionType
import com.example.budget.databinding.FragmentAddExpenseBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModel.ExpenseViewModelFactory((requireActivity().application as BudgetApplication).repository)
    }

    private var selectedDate: Long = System.currentTimeMillis()
    private var existingTransaction: Transaction? = null
    private var currentType = TransactionType.EXPENSE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Listen for scanner results
        setFragmentResultListener("scanner_result") { _, bundle ->
            val merchant = bundle.getString("merchant")
            val amount = bundle.getDouble("amount")
            
            if (!merchant.isNullOrEmpty()) {
                binding.etTitle.setText(merchant)
            }
            if (amount > 0) {
                // Format for the MoneyTextWatcher
                val formatted = NumberFormat.getCurrencyInstance(Locale.US).format(amount)
                binding.etAmount.setText(formatted)
            }
            Toast.makeText(requireContext(), "Ticket escaneado con éxito", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDatePicker()
        binding.etAmount.addTextChangedListener(MoneyTextWatcher(binding.etAmount))

        val transactionId = arguments?.getInt("transactionId", -1) ?: -1
        if (transactionId != -1) {
            viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
                existingTransaction = transactions.find { it.id == transactionId }
                existingTransaction?.let { 
                    currentType = it.type
                    populateFields(it) 
                    setupCategoryDropdown()
                }
            }
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnScanTicket.visibility = View.GONE // Don't show scanner on edit
        } else {
            setupCategoryDropdown()
        }

        binding.toggleGroupType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentType = if (checkedId == R.id.btnTypeIncome) TransactionType.INCOME else TransactionType.EXPENSE
                setupCategoryDropdown()
                binding.actvCategory.text.clear()
            }
        }

        binding.btnSave.setOnClickListener { saveTransaction() }
        binding.btnDelete.setOnClickListener { deleteTransaction() }
        binding.btnScanTicket.setOnClickListener {
            findNavController().navigate(R.id.action_addExpenseFragment_to_scannerFragment)
        }
        binding.btnManageCategories.setOnClickListener {
            findNavController().navigate(R.id.categoryManagerFragment)
        }
    }

    private fun setupCategoryDropdown() {
        viewModel.getCategoriesByType(currentType).observe(viewLifecycleOwner) { categories ->
            val names = categories.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
            binding.actvCategory.setAdapter(adapter)
        }
    }

    private fun setupDatePicker() {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etDate.setText(formatter.format(Date(selectedDate)))

        binding.etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona una fecha")
                .setSelection(selectedDate)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDate = selection
                binding.etDate.setText(formatter.format(Date(selectedDate)))
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    private fun populateFields(transaction: Transaction) {
        binding.etTitle.setText(transaction.title)
        
        val formattedAmount = NumberFormat.getCurrencyInstance(Locale.US).format(transaction.amount)
        binding.etAmount.setText(formattedAmount)
        
        binding.actvCategory.setText(transaction.category, false)
        binding.etNote.setText(transaction.note)
        selectedDate = transaction.date
        
        if (transaction.type == TransactionType.INCOME) {
            binding.toggleGroupType.check(R.id.btnTypeIncome)
        } else {
            binding.toggleGroupType.check(R.id.btnTypeExpense)
        }

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etDate.setText(formatter.format(Date(selectedDate)))
        binding.btnSave.text = "Actualizar"
    }

    private fun saveTransaction() {
        val title = binding.etTitle.text.toString()
        val amountStr = binding.etAmount.text.toString().replace("[$,]".toRegex(), "")
        val category = binding.actvCategory.text.toString()
        val note = binding.etNote.text.toString()
        val type = if (binding.toggleGroupType.checkedButtonId == R.id.btnTypeIncome) 
            TransactionType.INCOME else TransactionType.EXPENSE

        if (title.isNotEmpty() && amountStr.isNotEmpty() && category.isNotEmpty()) {
            val transaction = Transaction(
                id = existingTransaction?.id ?: 0,
                title = title,
                amount = amountStr.toDouble(),
                category = category,
                date = selectedDate,
                note = note,
                type = type
            )
            if (existingTransaction == null) {
                viewModel.insertTransaction(transaction)
            } else {
                viewModel.updateTransaction(transaction)
            }
            findNavController().popBackStack()
        } else {
            Toast.makeText(requireContext(), "Llena los campos obligatorios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTransaction() {
        existingTransaction?.let {
            viewModel.deleteTransaction(it)
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
