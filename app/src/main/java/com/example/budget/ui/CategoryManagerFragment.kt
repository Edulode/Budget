package com.example.budget.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budget.BudgetApplication
import com.example.budget.data.Category
import com.example.budget.databinding.FragmentCategoryManagerBinding

class CategoryManagerFragment : Fragment() {
    private var _binding: FragmentCategoryManagerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModel.ExpenseViewModelFactory((requireActivity().application as BudgetApplication).repository)
    }

    private var editingCategory: Category? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CategoryAdapter(
            onEdit = { category ->
                editingCategory = category
                binding.etCategoryName.setText(category.name)
                binding.btnAddCategory.text = "Actualizar"
            },
            onDelete = { category ->
                viewModel.deleteCategory(category)
            }
        )

        binding.rvCategories.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories)
        }

        binding.btnAddCategory.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()
            if (name.isNotEmpty()) {
                if (editingCategory == null) {
                    viewModel.insertCategory(Category(name = name))
                } else {
                    viewModel.updateCategory(editingCategory!!.copy(name = name))
                    editingCategory = null
                    binding.btnAddCategory.text = "Añadir"
                }
                binding.etCategoryName.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Escribe un nombre", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
