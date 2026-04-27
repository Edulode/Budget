package com.example.budget.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budget.BudgetApplication
import com.example.budget.R
import com.example.budget.data.CategorySummary
import com.example.budget.data.Transaction
import com.example.budget.data.TransactionType
import com.example.budget.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.NumberFormat
import java.util.*

class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModel.ExpenseViewModelFactory((requireActivity().application as BudgetApplication).repository)
    }

    private var currentType = TransactionType.EXPENSE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = StatisticsAdapter()
        binding.rvStatistics.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.toggleGroupStats.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentType = if (checkedId == R.id.btnStatsExpenses) TransactionType.EXPENSE else TransactionType.INCOME
                updateUI(adapter)
            }
        }

        updateUI(adapter)

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            updateMonthlyChart(transactions)
            calculateTrends(transactions)
        }
    }

    private fun updateUI(adapter: StatisticsAdapter) {
        if (currentType == TransactionType.EXPENSE) {
            binding.tvChartTitle.text = "Distribución de Gastos"
            viewModel.expenseCategorySummaries.observe(viewLifecycleOwner) { summaries ->
                if (currentType == TransactionType.EXPENSE) {
                    adapter.submitList(summaries)
                    updatePieChart(summaries)
                }
            }
        } else {
            binding.tvChartTitle.text = "Distribución de Ingresos"
            viewModel.incomeCategorySummaries.observe(viewLifecycleOwner) { summaries ->
                if (currentType == TransactionType.INCOME) {
                    adapter.submitList(summaries)
                    updatePieChart(summaries)
                }
            }
        }
    }

    private fun updatePieChart(summaries: List<CategorySummary>) {
        val entries = summaries.map { PieEntry(it.amount.toFloat(), it.category) }
        val dataSet = PieDataSet(entries, "")
        
        val colors = ArrayList<Int>()
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        
        dataSet.colors = colors
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.DKGRAY
        dataSet.valueFormatter = PercentFormatter(binding.pieChart)
        dataSet.sliceSpace = 3f

        binding.pieChart.apply {
            data = PieData(dataSet)
            setUsePercentValues(true)
            description.isEnabled = false
            legend.isEnabled = true
            legend.isWordWrapEnabled = true
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            
            setHoleColor(Color.TRANSPARENT)
            setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.text_main))
            setEntryLabelColor(Color.DKGRAY)
            setEntryLabelTextSize(10f)
            
            animateY(1000)
            invalidate()
        }
    }

    private fun updateMonthlyChart(transactions: List<Transaction>) {
        val monthlyData = transactions.groupBy {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.date
            val month = cal.get(Calendar.MONTH) + 1
            val year = cal.get(Calendar.YEAR)
            year * 100 + month
        }.mapValues { entry -> 
            val totalInc = entry.value.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExp = entry.value.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            Pair(totalInc, totalExp)
        }.toSortedMap()

        val incomeEntries = ArrayList<BarEntry>()
        val expenseEntries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        val lastMonths = monthlyData.toList().takeLast(6)
        lastMonths.forEachIndexed { index, entry ->
            incomeEntries.add(BarEntry(index.toFloat(), entry.second.first.toFloat()))
            expenseEntries.add(BarEntry(index.toFloat(), entry.second.second.toFloat()))
            val year = entry.first / 100
            val month = entry.first % 100
            labels.add("$month/$year")
        }

        val incomeSet = BarDataSet(incomeEntries, "Ingresos")
        incomeSet.color = Color.parseColor("#22C55E")
        incomeSet.setDrawValues(true)
        incomeSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        incomeSet.valueTextSize = 10f

        val expenseSet = BarDataSet(expenseEntries, "Gastos")
        expenseSet.color = Color.parseColor("#EF4444")
        expenseSet.setDrawValues(true)
        expenseSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        expenseSet.valueTextSize = 10f

        val barData = BarData(incomeSet, expenseSet)
        barData.barWidth = 0.35f

        binding.barChart.apply {
            data = barData
            
            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = labels.size.toFloat() - 0.5f
            groupBars(-0.5f, 0.2f, 0.05f) 
            
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setCenterAxisLabels(true)
            xAxis.setDrawGridLines(false)
            xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = Color.parseColor("#F1F5F9")
            axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            
            // Adjust Y axis so small bars are more visible relative to each other
            axisLeft.axisMinimum = 0f
            
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = true
            legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            
            animateY(1000)
            invalidate()
        }
    }

    private fun calculateTrends(transactions: List<Transaction>) {
        if (transactions.isEmpty()) return

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val thisMonthExpenses = transactions.filter {
            val eCal = Calendar.getInstance()
            eCal.timeInMillis = it.date
            it.type == TransactionType.EXPENSE && eCal.get(Calendar.MONTH) == currentMonth && eCal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }

        val thisMonthIncome = transactions.filter {
            val eCal = Calendar.getInstance()
            eCal.timeInMillis = it.date
            it.type == TransactionType.INCOME && eCal.get(Calendar.MONTH) == currentMonth && eCal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }

        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        
        binding.tvComparison.text = "Este mes:\nIngresos: ${format.format(thisMonthIncome)}\nGastos: ${format.format(thisMonthExpenses)}"

        val balance = thisMonthIncome - thisMonthExpenses
        binding.tvMonthlyTrend.text = "Balance del mes: ${format.format(balance)}"
        binding.tvMonthlyTrend.setTextColor(if (balance >= 0) Color.parseColor("#22C55E") else Color.parseColor("#EF4444"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
