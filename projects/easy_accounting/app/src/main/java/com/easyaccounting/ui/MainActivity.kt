package com.easyaccounting.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.easyaccounting.EasyAccountingApp
import com.easyaccounting.R
import com.easyaccounting.data.entity.Bill
import com.easyaccounting.data.entity.Income
import com.easyaccounting.databinding.ActivityMainBinding
import com.easyaccounting.ui.add.AddRecordActivity
import com.easyaccounting.ui.main.BillAdapter
import com.easyaccounting.ui.main.IncomeAdapter
import com.easyaccounting.ui.main.MainViewModel
import com.easyaccounting.ui.main.MainViewModelFactory
import com.easyaccounting.util.DateUtils
import com.easyaccounting.util.FormatUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels {
        val app = application as EasyAccountingApp
        MainViewModelFactory(
            app.billRepository,
            app.incomeRepository,
            app.categoryRepository
        )
    }

    private lateinit var billAdapter: BillAdapter
    private lateinit var incomeAdapter: IncomeAdapter

    private var currentTab = Tab.BILL // 默认显示账单

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupRecyclerView()
        setupMonthSelector()
        setupTabs()
        setupFab()
        setupSearch()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        // 每次返回时刷新数据
    }

    private fun setupViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "简易记账"

        // 饼图设置
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 50f
            transparentCircleRadius = 55f
            setDrawEntryLabels(false)
            legend.isEnabled = true
        }

        // 折线图设置
        binding.lineChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            legend.isEnabled = true
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
        }
    }

    private fun setupRecyclerView() {
        billAdapter = BillAdapter(
            onItemClick = { bill -> navigateToDetail(bill) },
            onItemLongClick = { bill -> showDeleteDialog(bill) }
        )
        incomeAdapter = IncomeAdapter(
            onItemClick = { income -> navigateToIncomeDetail(income) },
            onItemLongClick = { income -> showIncomeDeleteDialog(income) }
        )

        binding.rvRecords.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = billAdapter
        }
    }

    private fun setupMonthSelector() {
        binding.btnPreviousMonth.setOnClickListener {
            viewModel.previousMonth()
        }
        binding.btnNextMonth.setOnClickListener {
            viewModel.nextMonth()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        currentTab = Tab.BILL
                        binding.rvRecords.adapter = billAdapter
                        binding.tvRecordsTitle.text = "支出记录"
                        binding.rvRecords.visibility = View.VISIBLE
                        binding.tvRecordsTitle.visibility = View.VISIBLE
                        binding.statisticsContainer.visibility = View.GONE
                    }
                    1 -> {
                        currentTab = Tab.INCOME
                        binding.rvRecords.adapter = incomeAdapter
                        binding.tvRecordsTitle.text = "收入记录"
                        binding.rvRecords.visibility = View.VISIBLE
                        binding.tvRecordsTitle.visibility = View.VISIBLE
                        binding.statisticsContainer.visibility = View.GONE
                    }
                    2 -> {
                        currentTab = Tab.STATISTICS
                        binding.rvRecords.visibility = View.GONE
                        binding.tvRecordsTitle.visibility = View.GONE
                        binding.statisticsContainer.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, AddRecordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val keyword = binding.etSearch.text.toString()
            viewModel.setSearchKeyword(keyword)
            true
        }
    }

    private fun observeData() {
        // 观察月份
        lifecycleScope.launch {
            combine(viewModel.currentYear, viewModel.currentMonth) { year, month ->
                Pair(year, month)
            }.collect { (year, month) ->
                binding.tvCurrentMonth.text = DateUtils.formatMonth(year, month)
            }
        }

        // 观察月度统计
        lifecycleScope.launch {
            viewModel.monthlyStats.collect { stats ->
                binding.tvTotalExpense.text = FormatUtils.formatAmount(stats.totalExpense)
                binding.tvTotalIncome.text = FormatUtils.formatAmount(stats.totalIncome)
                binding.tvBalance.text = FormatUtils.formatAmount(stats.balance)
            }
        }

        // 观察账单列表
        lifecycleScope.launch {
            viewModel.bills.collect { bills ->
                billAdapter.submitList(bills)
            }
        }

        // 观察收入列表
        lifecycleScope.launch {
            viewModel.incomes.collect { incomes ->
                incomeAdapter.submitList(incomes)
            }
        }

        // 观察趋势数据
        lifecycleScope.launch {
            viewModel.trendData.collect { trends ->
                updateLineChart(trends)
            }
        }

        // 观察Top支出
        lifecycleScope.launch {
            viewModel.topExpenses.collect { topBills ->
                updatePieChart(topBills)
            }
        }
    }

    private fun updatePieChart(topBills: List<Bill>) {
        if (topBills.isEmpty()) {
            binding.pieChart.clear()
            return
        }

        val entries = topBills.map { bill ->
            PieEntry(bill.amount.toFloat(), bill.remark ?: "其他")
        }

        val colors = listOf(
            0xFF4CAF50.toInt(), // 绿
            0xFF2196F3.toInt(), // 蓝
            0xFFFF9800.toInt(), // 橙
            0xFFE91E63.toInt(), // 粉
            0xFF9C27B0.toInt()  // 紫
        )

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = 0xFFFFFFFF.toInt()
        }

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    private fun updateLineChart(trends: List<com.easyaccounting.ui.main.MonthlyTrend>) {
        if (trends.isEmpty()) {
            binding.lineChart.clear()
            return
        }

        val expenseEntries = trends.mapIndexed { index, trend ->
            Entry(index.toFloat(), trend.expense.toFloat())
        }

        val incomeEntries = trends.mapIndexed { index, trend ->
            Entry(index.toFloat(), trend.income.toFloat())
        }

        val labels = trends.map { "${it.month}月" }

        val expenseDataSet = LineDataSet(expenseEntries, "支出").apply {
            color = 0xFFE53935.toInt()
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(false)
        }

        val incomeDataSet = LineDataSet(incomeEntries, "收入").apply {
            color = 0xFF43A047.toInt()
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(false)
        }

        binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.lineChart.data = LineData(expenseDataSet, incomeDataSet)
        binding.lineChart.invalidate()
    }

    private fun navigateToDetail(billWithCategory: com.easyaccounting.data.entity.BillWithCategory) {
        val intent = Intent(this, RecordDetailActivity::class.java).apply {
            putExtra(RecordDetailActivity.EXTRA_BILL_ID, billWithCategory.id)
        }
        startActivity(intent)
    }

    private fun navigateToIncomeDetail(income: Income) {
        val intent = Intent(this, RecordDetailActivity::class.java).apply {
            putExtra(RecordDetailActivity.EXTRA_INCOME_ID, income.id)
        }
        startActivity(intent)
    }

    private fun showDeleteDialog(billWithCategory: com.easyaccounting.data.entity.BillWithCategory) {
        AlertDialog.Builder(this)
            .setTitle("删除确认")
            .setMessage("确定要删除这条支出记录吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteBill(billWithCategory.toBill())
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showIncomeDeleteDialog(income: Income) {
        AlertDialog.Builder(this)
            .setTitle("删除确认")
            .setMessage("确定要删除这条收入记录吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteIncome(income)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private enum class Tab {
        BILL, INCOME, STATISTICS
    }
}
