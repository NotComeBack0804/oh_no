package com.easyaccounting.ui.main

import androidx.lifecycle.*
import com.easyaccounting.data.entity.Bill
import com.easyaccounting.data.entity.BillWithCategory
import com.easyaccounting.data.entity.Category
import com.easyaccounting.data.entity.CategoryType
import com.easyaccounting.data.entity.Income
import com.easyaccounting.data.repository.BillRepository
import com.easyaccounting.data.repository.CategoryRepository
import com.easyaccounting.data.repository.IncomeRepository
import com.easyaccounting.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val billRepository: BillRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // 当前选中的年份和月份
    private val _currentYear = MutableStateFlow(DateUtils.getCurrentYear())
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    private val _currentMonth = MutableStateFlow(DateUtils.getCurrentMonth())
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()

    // 选中的分类ID列表（用于筛选）
    private val _selectedCategoryIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCategoryIds: StateFlow<Set<Long>> = _selectedCategoryIds.asStateFlow()

    // 搜索关键词
    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    // 当前月份的时间范围
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMonthRange: Flow<Pair<Long, Long>> = combine(_currentYear, _currentMonth) { year, month ->
        Pair(DateUtils.getStartOfMonth(year, month), DateUtils.getEndOfMonth(year, month))
    }

    // 月度统计
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyStats: Flow<MonthlyStats> = currentMonthRange.flatMapLatest { (start, end) ->
        combine(
            billRepository.getTotalExpenseByDateRange(start, end),
            incomeRepository.getTotalIncomeByDateRange(start, end)
        ) { expense, income ->
            MonthlyStats(
                totalExpense = expense ?: 0.0,
                totalIncome = income ?: 0.0,
                balance = (income ?: 0.0) - (expense ?: 0.0)
            )
        }
    }

    // 账单列表（按筛选条件），使用 JOIN 查询包含分类名称
    @OptIn(ExperimentalCoroutinesApi::class)
    val bills: Flow<List<BillWithCategory>> = combine(
        currentMonthRange,
        _selectedCategoryIds,
        _searchKeyword
    ) { (start, end), categoryIds, keyword ->
        Triple(start, end, Pair(categoryIds, keyword))
    }.flatMapLatest { (start, end, filter) ->
        val (categoryIds, keyword) = filter
        when {
            keyword.isNotBlank() -> billRepository.searchBillsWithCategory(keyword)
                .map { bills -> if (categoryIds.isNotEmpty()) bills.filter { it.categoryId in categoryIds } else bills }
            categoryIds.isNotEmpty() -> billRepository.getBillsWithCategoryByDateRange(start, end)
                .map { bills -> bills.filter { it.categoryId in categoryIds } }
            else -> billRepository.getBillsWithCategoryByDateRange(start, end)
        }
    }

    // 收入列表
    @OptIn(ExperimentalCoroutinesApi::class)
    val incomes: Flow<List<Income>> = currentMonthRange.flatMapLatest { (start, end) ->
        incomeRepository.getIncomesByDateRange(start, end)
    }

    // 分类列表
    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: Flow<List<Category>> = _currentYear.flatMapLatest {
        categoryRepository.getAllCategories()
    }

    // 支出分类
    val expenseCategories: Flow<List<Category>> = flow {
        emit(categoryRepository.getCategoriesByTypeSync(CategoryType.EXPENSE))
    }.flatMapLatest { categoryList ->
        flow { emit(categoryList) }
    }

    // 收入分类
    val incomeCategories: Flow<List<Category>> = flow {
        emit(categoryRepository.getCategoriesByTypeSync(CategoryType.INCOME))
    }.flatMapLatest { categoryList ->
        flow { emit(categoryList) }
    }

    // 近6个月的趋势数据
    val trendData: Flow<List<MonthlyTrend>> = flow {
        val trends = mutableListOf<MonthlyTrend>()
        val calendar = java.util.Calendar.getInstance()
        for (monthOffset in listOf(5, 4, 3, 2, 1, 0)) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(java.util.Calendar.MONTH, -monthOffset)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis
            calendar.set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            val end = calendar.timeInMillis
            val expense = billRepository.getBillsByDateRangeSync(start, end).sumOf { it.amount }
            val income = incomeRepository.getIncomesByDateRangeSync(start, end).sumOf { it.amount }
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            trends.add(MonthlyTrend(year = year, month = month, expense = expense, income = income))
        }
        emit(trends)
    }

    // Top5 支出排行榜
    @OptIn(ExperimentalCoroutinesApi::class)
    val topExpenses: Flow<List<Bill>> = currentMonthRange.flatMapLatest { (start, end) ->
        billRepository.getTopBillsByAmount(start, end, 5)
    }

    init {
        // 初始化月份范围
        viewModelScope.launch {
            _currentYear.value = DateUtils.getCurrentYear()
            _currentMonth.value = DateUtils.getCurrentMonth()
        }
    }

    fun setYearMonth(year: Int, month: Int) {
        _currentYear.value = year
        _currentMonth.value = month
    }

    fun previousMonth() {
        if (_currentMonth.value == 1) {
            _currentMonth.value = 12
            _currentYear.value -= 1
        } else {
            _currentMonth.value -= 1
        }
    }

    fun nextMonth() {
        if (_currentMonth.value == 12) {
            _currentMonth.value = 1
            _currentYear.value += 1
        } else {
            _currentMonth.value += 1
        }
    }

    fun setSelectedCategories(categoryIds: Set<Long>) {
        _selectedCategoryIds.value = categoryIds
    }

    fun setSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
    }

    fun deleteBill(bill: Bill) {
        viewModelScope.launch {
            billRepository.deleteBill(bill)
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            incomeRepository.deleteIncome(income)
        }
    }
}

data class MonthlyStats(
    val totalExpense: Double,
    val totalIncome: Double,
    val balance: Double
)

data class MonthlyTrend(
    val year: Int,
    val month: Int,
    val expense: Double,
    val income: Double
)

class MainViewModelFactory(
    private val billRepository: BillRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(billRepository, incomeRepository, categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
