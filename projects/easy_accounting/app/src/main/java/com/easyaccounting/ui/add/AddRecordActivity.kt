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
import com.easyaccounting.util.ThemeUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

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
        ThemeUtils.applySelectedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ThemeUtils.applyScreenBackground(binding.addRecordRoot, this)

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
        supportActionBar?.title = "新增记录"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupTypeSwitch() {
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btn_expense -> viewModel.setRecordType(RecordType.EXPENSE)
                R.id.btn_income -> viewModel.setRecordType(RecordType.INCOME)
            }
        }
    }

    private fun setupAmountInput() {
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

                if (digit == "." && current.contains(".")) return@setOnClickListener

                val parts = current.split(".")
                if (parts.size == 2 && parts[1].length >= 2 && digit != ".") return@setOnClickListener
                if (current.length >= 10 && digit != ".") return@setOnClickListener

                viewModel.setAmount(current + digit)
            }
        }

        binding.btnDelete.setOnClickListener {
            val current = viewModel.amount.value
            if (current.isNotEmpty()) {
                viewModel.setAmount(current.dropLast(1))
            }
        }

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
            val calendar = Calendar.getInstance().apply {
                timeInMillis = viewModel.selectedDate.value
            }

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    viewModel.setSelectedDate(selectedCalendar.timeInMillis)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupRemark() {
        binding.etRemark.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.setRemark(s?.toString().orEmpty())
            }
        })
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener { viewModel.save() }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.recordType.collectLatest { type ->
                updateUIForType(type)
            }
        }

        lifecycleScope.launch {
            viewModel.amount.collectLatest { amount ->
                binding.tvAmount.text = amount.ifEmpty { "0.00" }
            }
        }

        lifecycleScope.launch {
            viewModel.selectedDate.collectLatest { date ->
                binding.btnDate.text = DateUtils.formatDate(date)
            }
        }

        lifecycleScope.launch {
            viewModel.expenseCategories.collectLatest { categories ->
                if (viewModel.recordType.value == RecordType.EXPENSE) {
                    categoryAdapter.submitList(categories)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.incomeCategories.collectLatest { categories ->
                if (viewModel.recordType.value == RecordType.INCOME) {
                    categoryAdapter.submitList(categories)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.accounts.collectLatest { accounts ->
                accountAdapter.submitList(accounts)
            }
        }

        viewModel.saveResult.observe(this) { result ->
            result ?: return@observe
            when (result) {
                is SaveResult.Success -> {
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                    finish()
                }

                is SaveResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.clearSaveResult()
        }

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.btnSave.isEnabled = !isLoading
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isDataLoaded.collectLatest { isLoaded ->
                binding.btnSave.isEnabled = isLoaded && !viewModel.isLoading.value
            }
        }
    }

    private fun updateUIForType(type: RecordType) {
        when (type) {
            RecordType.EXPENSE -> {
                binding.tvAmountHint.text = "支出金额"
                binding.tvCategoryLabel.text = "选择分类"
                binding.amountPanel.setBackgroundResource(R.drawable.bg_amount_expense)
                binding.btnSave.text = "保存支出"
                categoryAdapter.submitList(viewModel.expenseCategories.value)
            }

            RecordType.INCOME -> {
                binding.tvAmountHint.text = "收入金额"
                binding.tvCategoryLabel.text = "选择来源"
                binding.amountPanel.setBackgroundResource(R.drawable.bg_amount_income)
                binding.btnSave.text = "保存收入"
                categoryAdapter.submitList(viewModel.incomeCategories.value)
            }
        }
        categoryAdapter.setSelectedCategory(null)
    }
}
