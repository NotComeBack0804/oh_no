package com.easyaccounting.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.easyaccounting.EasyAccountingApp
import com.easyaccounting.R
import com.easyaccounting.data.entity.Bill
import com.easyaccounting.data.entity.Income
import com.easyaccounting.databinding.ActivityRecordDetailBinding
import com.easyaccounting.ui.ai.AiChatActivity
import com.easyaccounting.util.DateUtils
import com.easyaccounting.util.FormatUtils
import com.easyaccounting.util.ThemeUtils
import kotlinx.coroutines.launch

class RecordDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordDetailBinding

    private val app by lazy { application as EasyAccountingApp }

    private var billId: Long? = null
    private var incomeId: Long? = null
    private var isBill = true

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySelectedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ThemeUtils.applyScreenBackground(binding.detailRoot, this)

        billId = intent.getLongExtra(EXTRA_BILL_ID, -1L).takeIf { it != -1L }
        incomeId = intent.getLongExtra(EXTRA_INCOME_ID, -1L).takeIf { it != -1L }

        when {
            billId != null -> {
                isBill = true
                loadBillDetails()
            }

            incomeId != null -> {
                isBill = false
                loadIncomeDetails()
            }

            else -> {
                finish()
                return
            }
        }

        setupToolbar()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isBill) "支出详情" else "收入详情"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupButtons() {
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }
    }

    private fun loadBillDetails() {
        lifecycleScope.launch {
            billId?.let { id ->
                val bill = app.billRepository.getBillById(id) ?: return@launch
                displayBill(bill)

                binding.tvCategory.text = bill.categoryId?.let { categoryId ->
                    app.categoryRepository.getCategoryById(categoryId)?.name ?: "未知分类"
                } ?: "未分类"

                binding.tvAccount.text = bill.accountId?.let { accountId ->
                    app.accountRepository.getAccountById(accountId)?.name ?: "未知账户"
                } ?: "未设置账户"

                val messageId = app.aiChatRepository.findFirstMessageIdByBillId(id)
                updateContextButton(
                    targetMessageId = messageId,
                    linkedBillId = id,
                    linkedIncomeId = null
                )
            }
        }
    }

    private fun loadIncomeDetails() {
        lifecycleScope.launch {
            incomeId?.let { id ->
                val income = app.incomeRepository.getIncomeById(id) ?: return@launch
                displayIncome(income)

                binding.tvAccount.text = income.accountId?.let { accountId ->
                    app.accountRepository.getAccountById(accountId)?.name ?: "未知账户"
                } ?: "未设置账户"

                val messageId = app.aiChatRepository.findFirstMessageIdByIncomeId(id)
                updateContextButton(
                    targetMessageId = messageId,
                    linkedBillId = null,
                    linkedIncomeId = id
                )
            }
        }
    }

    private fun updateContextButton(
        targetMessageId: Long?,
        linkedBillId: Long?,
        linkedIncomeId: Long?
    ) {
        binding.btnViewContext.isVisible = targetMessageId != null
        if (targetMessageId == null) return

        binding.btnViewContext.setOnClickListener {
            startActivity(
                Intent(this, AiChatActivity::class.java).apply {
                    putExtra(AiChatActivity.EXTRA_TARGET_MESSAGE_ID, targetMessageId)
                    linkedBillId?.let { putExtra(AiChatActivity.EXTRA_LINKED_BILL_ID, it) }
                    linkedIncomeId?.let { putExtra(AiChatActivity.EXTRA_LINKED_INCOME_ID, it) }
                }
            )
        }
    }

    private fun displayBill(bill: Bill) {
        binding.amountPanel.setBackgroundResource(R.drawable.bg_amount_expense)
        binding.tvAmount.text = "- ${FormatUtils.formatAmount(bill.amount)}"
        binding.tvDate.text = DateUtils.formatDate(bill.date)
        binding.tvRemark.text = bill.remark?.takeIf { it.isNotBlank() } ?: "未填写备注"
        binding.tvType.text = "支出"
        binding.tvSourceOrCategory.text = "分类"
        binding.tvCategory.text = "加载中..."
        binding.tvAccount.text = "加载中..."
        binding.btnDelete.text = "删除支出记录"
    }

    private fun displayIncome(income: Income) {
        binding.amountPanel.setBackgroundResource(R.drawable.bg_amount_income)
        binding.tvAmount.text = "+ ${FormatUtils.formatAmount(income.amount)}"
        binding.tvDate.text = DateUtils.formatDate(income.date)
        binding.tvRemark.text = income.remark?.takeIf { it.isNotBlank() } ?: "未填写备注"
        binding.tvType.text = "收入"
        binding.tvSourceOrCategory.text = "来源"
        binding.tvCategory.text = income.source
        binding.tvAccount.text = "加载中..."
        binding.btnDelete.text = "删除收入记录"
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("删除确认")
            .setMessage("确定要删除这条记录吗？")
            .setPositiveButton("删除") { _, _ -> deleteRecord() }
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
                Toast.makeText(
                    this@RecordDetailActivity,
                    "删除失败：${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        const val EXTRA_BILL_ID = "extra_bill_id"
        const val EXTRA_INCOME_ID = "extra_income_id"
    }
}
