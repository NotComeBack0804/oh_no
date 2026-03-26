package com.easyaccounting.ui.add

import androidx.lifecycle.*
import com.easyaccounting.data.entity.Account
import com.easyaccounting.data.entity.Bill
import com.easyaccounting.data.entity.Category
import com.easyaccounting.data.entity.CategoryType
import com.easyaccounting.data.entity.Income
import com.easyaccounting.data.repository.AccountRepository
import com.easyaccounting.data.repository.BillRepository
import com.easyaccounting.data.repository.CategoryRepository
import com.easyaccounting.data.repository.IncomeRepository
import com.easyaccounting.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddRecordViewModel(
    private val billRepository: BillRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    // 记录类型：支出/收入
    private val _recordType = MutableStateFlow(RecordType.EXPENSE)
    val recordType: StateFlow<RecordType> = _recordType.asStateFlow()

    // 金额
    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    // 选中的分类
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    // 选中的账户
    private val _selectedAccount = MutableStateFlow<Account?>(null)
    val selectedAccount: StateFlow<Account?> = _selectedAccount.asStateFlow()

    // 日期
    private val _selectedDate = MutableStateFlow(DateUtils.getTodayStart())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    // 备注
    private val _remark = MutableStateFlow("")
    val remark: StateFlow<String> = _remark.asStateFlow()

    // 收入来源（用于收入记录）
    private val _incomeSource = MutableStateFlow("")
    val incomeSource: StateFlow<String> = _incomeSource.asStateFlow()

    // 支出分类列表
    private val _expenseCategories = MutableStateFlow<List<Category>>(emptyList())
    val expenseCategories: StateFlow<List<Category>> = _expenseCategories.asStateFlow()

    // 收入分类列表
    private val _incomeCategories = MutableStateFlow<List<Category>>(emptyList())
    val incomeCategories: StateFlow<List<Category>> = _incomeCategories.asStateFlow()

    // 账户列表
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    // 保存结果
    private val _saveResult = MutableLiveData<SaveResult?>()
    val saveResult: LiveData<SaveResult?> = _saveResult

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 数据是否已加载完成
    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // 加载支出分类
                _expenseCategories.value = categoryRepository.getCategoriesByTypeSync(CategoryType.EXPENSE)
                // 加载收入分类
                _incomeCategories.value = categoryRepository.getCategoriesByTypeSync(CategoryType.INCOME)
                // 加载账户
                _accounts.value = accountRepository.getAllAccountsSync()
            } finally {
                _isDataLoaded.value = true
            }
        }
    }

    fun setRecordType(type: RecordType) {
        _recordType.value = type
        _selectedCategory.value = null
    }

    fun setAmount(amount: String) {
        _amount.value = amount
    }

    fun setSelectedCategory(category: Category) {
        _selectedCategory.value = category
    }

    fun setSelectedAccount(account: Account) {
        _selectedAccount.value = account
    }

    fun setSelectedDate(date: Long) {
        _selectedDate.value = date
    }

    fun setRemark(remark: String) {
        _remark.value = remark
    }

    fun setIncomeSource(source: String) {
        _incomeSource.value = source
    }

    fun save() {
        // 先在主线程做快速验证
        val amountStr = _amount.value
        if (amountStr.isNullOrBlank()) {
            _saveResult.value = SaveResult.Error("请输入金额")
            return
        }
        val amountValue = amountStr.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            _saveResult.value = SaveResult.Error("请输入有效金额")
            return
        }
        if (_selectedCategory.value == null) {
            _saveResult.value = SaveResult.Error("请选择分类")
            return
        }

        // 启动后台任务进行保存
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    when (_recordType.value) {
                        RecordType.EXPENSE -> saveExpenseToDb(amountValue)
                        RecordType.INCOME -> saveIncomeToDb(amountValue)
                    }
                }
                _saveResult.value = result
            } catch (e: Exception) {
                _saveResult.value = SaveResult.Error(e.message ?: "保存失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun saveExpenseToDb(amount: Double): SaveResult {
        val categoryId = _selectedCategory.value?.id
        val accountId = _selectedAccount.value?.id
        val remark = _remark.value.takeIf { it.isNotBlank() }

        val bill = Bill(
            amount = amount,
            categoryId = categoryId,
            date = _selectedDate.value,
            remark = remark,
            accountId = accountId
        )
        billRepository.insertBill(bill)
        return SaveResult.Success
    }

    private suspend fun saveIncomeToDb(amount: Double): SaveResult {
        val source = _selectedCategory.value?.name ?: _incomeSource.value
        if (source.isBlank()) {
            return SaveResult.Error("请选择或输入收入来源")
        }
        val accountId = _selectedAccount.value?.id
        val remark = _remark.value.takeIf { it.isNotBlank() }

        val income = Income(
            amount = amount,
            source = source,
            date = _selectedDate.value,
            remark = remark,
            accountId = accountId
        )
        incomeRepository.insertIncome(income)
        return SaveResult.Success
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }
}

enum class RecordType {
    EXPENSE,
    INCOME
}

sealed class SaveResult {
    object Success : SaveResult()
    data class Error(val message: String) : SaveResult()
}

class AddRecordViewModelFactory(
    private val billRepository: BillRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddRecordViewModel::class.java)) {
            return AddRecordViewModel(
                billRepository,
                incomeRepository,
                categoryRepository,
                accountRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
