package com.easyaccounting.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.easyaccounting.EasyAccountingApp
import com.easyaccounting.R
import com.easyaccounting.databinding.ActivityAddRecordBinding
import com.easyaccounting.ui.main.AccountAdapter
import com.easyaccounting.ui.main.CategoryAdapter
import com.easyaccounting.util.DateUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class AddRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecordBinding

    private val viewModel: AddRecordViewModel by viewModels {
        val app = application as EasyAccountingApp
        AddRecordViewModelFactory(
            app.billRepository,
            app.incomeRepository,
            app.categoryRepository,
            app.accountRepository
        )
    }

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var accountAdapter: AccountAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTypeSwitch()
        setupAmountInput()
        setupCategorySelector()
        setupAccountSelector()
        setupDatePicker()
        setupRemark()
        setupSaveButton()
        observeData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "记账"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupTypeSwitch() {
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_expense -> viewModel.setRecordType(RecordType.EXPENSE)
                    R.id.btn_income -> viewModel.setRecordType(RecordType.INCOME)
                }
            }
        }
    }

    private fun setupAmountInput() {
        // 数字键盘
        val numberButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3,
            binding.btn4, binding.btn5, binding.btn6, binding.btn7,
            binding.btn8, binding.btn9, binding.btn00, binding.btnDot
        )

        numberButtons.forEach { button ->
            button.setOnClickListener {
                val current = viewModel.amount.value
                val digit = when (button) {
                    binding.btn0 -> "0"
                    binding.btn1 -> "1"
                    binding.btn2 -> "2"
                    binding.btn3 -> "3"
                    binding.btn4 -> "4"
                    binding.btn5 -> "5"
                    binding.btn6 -> "6"
                    binding.btn7 -> "7"
                    binding.btn8 -> "8"
                    binding.btn9 -> "9"
                    binding.btn00 -> "00"
                    binding.btnDot -> "."
                    else -> ""
                }

                // 防止重复小数点
                if (digit == "." && current.contains(".")) return@setOnClickListener

                // 限制小数位数为2位
                val parts = current.split(".")
                if (parts.size == 2 && parts[1].length >= 2 && digit != ".") return@setOnClickListener

                // 限制金额最大长度
                if (current.length >= 10 && digit != ".") return@setOnClickListener

                viewModel.setAmount(current + digit)
            }
        }

        // 删除按钮
        binding.btnDelete.setOnClickListener {
            val current = viewModel.amount.value
            if (current.isNotEmpty()) {
                viewModel.setAmount(current.dropLast(1))
            }
        }

        // 清空按钮
        binding.btnClear.setOnClickListener {
            viewModel.setAmount("")
        }
    }

    private fun setupCategorySelector() {
        categoryAdapter = CategoryAdapter { category ->
            viewModel.setSelectedCategory(category)
            categoryAdapter.setSelectedCategory(category.id)
        }

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(this@AddRecordActivity, 4)
            adapter = categoryAdapter
        }
    }

    private fun setupAccountSelector() {
        accountAdapter = AccountAdapter { account ->
            viewModel.setSelectedAccount(account)
            accountAdapter.setSelectedAccount(account.id)
        }

        binding.rvAccounts.apply {
            layoutManager = GridLayoutManager(this@AddRecordActivity, 4)
            adapter = accountAdapter
        }
    }

    private fun setupDatePicker() {
        binding.btnDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = viewModel.selectedDate.value

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0)
                    selectedCalendar.set(Calendar.MILLISECOND, 0)
                    viewModel.setSelectedDate(selectedCalendar.timeInMillis)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupRemark() {
        binding.etRemark.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.setRemark(binding.etRemark.text.toString())
            }
        }
        // Update on text change using TextWatcher
        binding.etRemark.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.setRemark(s.toString())
            }
        })
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            viewModel.save()
        }
    }

    private fun observeData() {
        // 观察记录类型
        lifecycleScope.launch {
            viewModel.recordType.collectLatest { type ->
                updateUIForType(type)
            }
        }

        // 观察金额
        lifecycleScope.launch {
            viewModel.amount.collectLatest { amount ->
                binding.tvAmount.text = if (amount.isEmpty()) "0.00" else amount
            }
        }

        // 观察日期
        lifecycleScope.launch {
            viewModel.selectedDate.collectLatest { date ->
                binding.btnDate.text = DateUtils.formatDate(date)
            }
        }

        // 观察支出分类
        lifecycleScope.launch {
            viewModel.expenseCategories.collectLatest { categories ->
                if (viewModel.recordType.value == RecordType.EXPENSE) {
                    categoryAdapter.submitList(categories)
                }
            }
        }

        // 观察收入分类
        lifecycleScope.launch {
            viewModel.incomeCategories.collectLatest { categories ->
                if (viewModel.recordType.value == RecordType.INCOME) {
                    categoryAdapter.submitList(categories)
                }
            }
        }

        // 观察账户列表
        lifecycleScope.launch {
            viewModel.accounts.collectLatest { accounts ->
                accountAdapter.submitList(accounts)
            }
        }

        // 观察保存结果
        viewModel.saveResult.observe(this) { result ->
            when (result) {
                is SaveResult.Success -> {
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is SaveResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
                null -> {}
            }
            viewModel.clearSaveResult()
        }

        // 观察加载状态
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.btnSave.isEnabled = !isLoading
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateUIForType(type: RecordType) {
        when (type) {
            RecordType.EXPENSE -> {
                binding.tvCategoryLabel.text = "选择分类"
                lifecycleScope.launch {
                    viewModel.expenseCategories.collectLatest { categories ->
                        categoryAdapter.submitList(categories)
                    }
                }
            }
            RecordType.INCOME -> {
                binding.tvCategoryLabel.text = "选择来源"
                lifecycleScope.launch {
                    viewModel.incomeCategories.collectLatest { categories ->
                        categoryAdapter.submitList(categories)
                    }
                }
            }
        }
        categoryAdapter.setSelectedCategory(null)
    }
}
