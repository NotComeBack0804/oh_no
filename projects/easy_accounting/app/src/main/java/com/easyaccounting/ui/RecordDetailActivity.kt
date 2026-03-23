package com.easyaccounting.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.easyaccounting.EasyAccountingApp
import com.easyaccounting.data.entity.Bill
import com.easyaccounting.data.entity.Income
import com.easyaccounting.databinding.ActivityRecordDetailBinding
import com.easyaccounting.util.DateUtils
import com.easyaccounting.util.FormatUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecordDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordDetailBinding

    private val app by lazy { application as EasyAccountingApp }

    private var billId: Long? = null
    private var incomeId: Long? = null
    private var isBill: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        billId = intent.getLongExtra(EXTRA_BILL_ID, -1).takeIf { it != -1L }
        incomeId = intent.getLongExtra(EXTRA_INCOME_ID, -1).takeIf { it != -1L }

        if (billId != null) {
            isBill = true
            loadBillDetails()
        } else if (incomeId != null) {
            isBill = false
            loadIncomeDetails()
        } else {
            finish()
            return
        }

        setupToolbar()
        setupDeleteButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isBill) "支出详情" else "收入详情"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDeleteButton() {
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadBillDetails() {
        lifecycleScope.launch {
            billId?.let { id ->
                val bill = app.billRepository.getBillById(id)
                bill?.let { displayBill(it) }

                // 获取分类名称
                bill?.categoryId?.let { categoryId ->
                    val category = app.categoryRepository.getCategoryById(categoryId)
                    binding.tvCategory.text = category?.name ?: "未知分类"
                }

                // 获取账户名称
                bill?.accountId?.let { accountId ->
                    val account = app.accountRepository.getAccountById(accountId)
                    binding.tvAccount.text = account?.name ?: "未知账户"
                }
            }
        }
    }

    private fun loadIncomeDetails() {
        lifecycleScope.launch {
            incomeId?.let { id ->
                val income = app.incomeRepository.getIncomeById(id)
                income?.let { displayIncome(it) }

                // 获取账户名称
                income?.accountId?.let { accountId ->
                    val account = app.accountRepository.getAccountById(accountId)
                    binding.tvAccount.text = account?.name ?: "未知账户"
                }
            }
        }
    }

    private fun displayBill(bill: Bill) {
        binding.tvAmount.text = "- ${FormatUtils.formatAmount(bill.amount)}"
        binding.tvDate.text = DateUtils.formatDate(bill.date)
        binding.tvRemark.text = bill.remark ?: "无备注"
        binding.tvType.text = "支出"
        binding.tvSourceOrCategory.text = "分类"
        binding.tvCategory.text = "" // 单独加载
    }

    private fun displayIncome(income: Income) {
        binding.tvAmount.text = "+ ${FormatUtils.formatAmount(income.amount)}"
        binding.tvDate.text = DateUtils.formatDate(income.date)
        binding.tvRemark.text = income.remark ?: "无备注"
        binding.tvType.text = "收入"
        binding.tvSourceOrCategory.text = "来源"
        binding.tvCategory.text = income.source
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("删除确认")
            .setMessage("确定要删除这条记录吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteRecord()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteRecord() {
        lifecycleScope.launch {
            try {
                if (isBill) {
                    billId?.let { id ->
                        app.billRepository.getBillById(id)?.let { bill ->
                            app.billRepository.deleteBill(bill)
                        }
                    }
                } else {
                    incomeId?.let { id ->
                        app.incomeRepository.getIncomeById(id)?.let { income ->
                            app.incomeRepository.deleteIncome(income)
                        }
                    }
                }
                Toast.makeText(this@RecordDetailActivity, "删除成功", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@RecordDetailActivity, "删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_BILL_ID = "extra_bill_id"
        const val EXTRA_INCOME_ID = "extra_income_id"
    }
}
