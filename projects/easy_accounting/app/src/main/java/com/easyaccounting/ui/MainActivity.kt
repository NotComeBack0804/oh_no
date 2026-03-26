package com.easyaccounting.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.easyaccounting.EasyAccountingApp
import com.easyaccounting.R
import com.easyaccounting.data.entity.BillWithCategory
import com.easyaccounting.data.entity.ExpenseCategoryShare
import com.easyaccounting.data.entity.Income
import com.easyaccounting.databinding.ActivityMainBinding
import com.easyaccounting.ui.add.AddRecordActivity
import com.easyaccounting.ui.ai.AiChatActivity
import com.easyaccounting.ui.main.BillAdapter
import com.easyaccounting.ui.main.IncomeAdapter
import com.easyaccounting.ui.main.MainViewModel
import com.easyaccounting.ui.main.MainViewModelFactory
import com.easyaccounting.ui.main.MonthlyTrend
import com.easyaccounting.ui.setting.AccessibilitySettingActivity
import com.easyaccounting.util.AppPreferences
import com.easyaccounting.util.DateUtils
import com.easyaccounting.util.FormatUtils
import com.easyaccounting.util.ThemeUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.R as MaterialAttr
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.flow.combine
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

    private var appliedThemeName = AppPreferences.DEFAULT_THEME

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySelectedTheme(this)
        appliedThemeName = AppPreferences.getSelectedThemeName(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ThemeUtils.applyScreenBackground(binding.mainRoot, this)

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
        val currentThemeName = AppPreferences.getSelectedThemeName(this)
        if (currentThemeName != appliedThemeName) {
            appliedThemeName = currentThemeName
            recreate()
        }
    }

    private fun setupViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        val onSurface = MaterialColors.getColor(binding.root, MaterialAttr.attr.colorOnSurface)
        val surface = MaterialColors.getColor(binding.root, MaterialAttr.attr.colorSurface)
        val primary = MaterialColors.getColor(binding.root, MaterialAttr.attr.colorPrimary)
        val outline = MaterialColors.getColor(binding.root, MaterialAttr.attr.colorOutlineVariant)

        binding.tabLayout.setSelectedTabIndicatorColor(primary)

        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 58f
            transparentCircleRadius = 64f
            setDrawEntryLabels(false)
            setHoleColor(surface)
            setCenterText("本月支出结构")
            setCenterTextSize(12f)
            setCenterTextColor(onSurface)
            legend.isEnabled = true
            legend.textColor = onSurface
            setNoDataText("暂无分类支出数据")
            setNoDataTextColor(onSurface)
        }

        binding.lineChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            legend.isEnabled = true
            legend.textColor = onSurface
            axisRight.isEnabled = false
            axisLeft.textColor = onSurface
            axisLeft.gridColor = outline
            axisLeft.axisMinimum = 0f
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = onSurface
            xAxis.gridColor = outline
            xAxis.setDrawGridLines(false)
            setNoDataText("暂无趋势数据")
            setNoDataTextColor(onSurface)
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
        binding.btnPreviousMonth.setOnClickListener { viewModel.previousMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.rvRecords.adapter = billAdapter
                        binding.tvRecordsTitle.text = "支出记录"
                        binding.rvRecords.visibility = View.VISIBLE
                        binding.tvRecordsTitle.visibility = View.VISIBLE
                        binding.statisticsContainer.visibility = View.GONE
                    }

                    1 -> {
                        binding.rvRecords.adapter = incomeAdapter
                        binding.tvRecordsTitle.text = "收入记录"
                        binding.rvRecords.visibility = View.VISIBLE
                        binding.tvRecordsTitle.visibility = View.VISIBLE
                        binding.statisticsContainer.visibility = View.GONE
                    }

                    2 -> {
                        binding.rvRecords.visibility = View.GONE
                        binding.tvRecordsTitle.visibility = View.GONE
                        binding.statisticsContainer.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) = Unit

            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) = Unit
        })
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddRecordActivity::class.java))
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            viewModel.setSearchKeyword(binding.etSearch.text?.toString().orEmpty())
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_ai_companion -> {
                startActivity(Intent(this, AiChatActivity::class.java))
                true
            }

            R.id.action_auto_accounting -> {
                startActivity(Intent(this, AccessibilitySettingActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            combine(viewModel.currentYear, viewModel.currentMonth) { year, month ->
                year to month
            }.collect { (year, month) ->
                binding.tvCurrentMonth.text = DateUtils.formatMonth(year, month)
            }
        }

        lifecycleScope.launch {
            viewModel.monthlyStats.collect { stats ->
                binding.tvTotalExpense.text = FormatUtils.formatAmount(stats.totalExpense)
                binding.tvTotalIncome.text = FormatUtils.formatAmount(stats.totalIncome)
                binding.tvBalance.text = FormatUtils.formatAmount(stats.balance)
            }
        }

        lifecycleScope.launch {
            viewModel.bills.collect { bills ->
                billAdapter.submitList(bills)
            }
        }

        lifecycleScope.launch {
            viewModel.incomes.collect { incomes ->
                incomeAdapter.submitList(incomes)
            }
        }

        lifecycleScope.launch {
            viewModel.trendData.collect { trends ->
                updateLineChart(trends)
            }
        }

        lifecycleScope.launch {
            viewModel.topExpenses.collect { categoryShares ->
                updatePieChart(categoryShares)
            }
        }
    }

    private fun updatePieChart(categoryShares: List<ExpenseCategoryShare>) {
        if (categoryShares.isEmpty()) {
            binding.pieChart.clear()
            return
        }

        val entries = categoryShares.map { categoryShare ->
            PieEntry(
                categoryShare.totalAmount.toFloat(),
                categoryShare.categoryName.ifBlank { "其他" }
            )
        }

        val colors = listOf(
            0xFFE85D75.toInt(),
            0xFF5F6EEF.toInt(),
            0xFF41B883.toInt(),
            0xFFFF9F43.toInt(),
            0xFF8E6CEF.toInt()
        )

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 4f
            selectionShift = 8f
            valueTextSize = 11f
            valueTextColor = 0xFFFFFFFF.toInt()
        }

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
        binding.pieChart.animateY(300)
    }

    private fun updateLineChart(trends: List<MonthlyTrend>) {
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
            color = 0xFFE85D75.toInt()
            lineWidth = 3f
            setCircleColor(color)
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        }

        val incomeDataSet = LineDataSet(incomeEntries, "收入").apply {
            color = 0xFF3AAE72.toInt()
            lineWidth = 3f
            setCircleColor(color)
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        }

        binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.lineChart.data = LineData(expenseDataSet, incomeDataSet)
        binding.lineChart.invalidate()
        binding.lineChart.animateX(300)
    }

    private fun navigateToDetail(billWithCategory: BillWithCategory) {
        startActivity(
            Intent(this, RecordDetailActivity::class.java).apply {
                putExtra(RecordDetailActivity.EXTRA_BILL_ID, billWithCategory.id)
            }
        )
    }

    private fun navigateToIncomeDetail(income: Income) {
        startActivity(
            Intent(this, RecordDetailActivity::class.java).apply {
                putExtra(RecordDetailActivity.EXTRA_INCOME_ID, income.id)
            }
        )
    }

    private fun showDeleteDialog(billWithCategory: BillWithCategory) {
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
}
