package com.easyaccounting.ui.ai

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.easyaccounting.EasyAccountingApp
import com.easyaccounting.R
import com.easyaccounting.databinding.ActivityAiChatBinding
import com.easyaccounting.ui.RecordDetailActivity
import com.easyaccounting.util.ThemeUtils
import kotlinx.coroutines.launch

class AiChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiChatBinding
    private lateinit var adapter: AiChatAdapter

    private val viewModel: AiChatViewModel by viewModels {
        val app = application as EasyAccountingApp
        AiChatViewModelFactory(
            appContext = applicationContext,
            aiChatRepository = app.aiChatRepository,
            billRepository = app.billRepository,
            incomeRepository = app.incomeRepository,
            categoryRepository = app.categoryRepository
        )
    }

    private var targetMessageId: Long? = null
    private var hasScrolledToTarget = false

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySelectedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityAiChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ThemeUtils.applyScreenBackground(binding.chatRoot, this)

        targetMessageId = intent.getLongExtra(EXTRA_TARGET_MESSAGE_ID, -1L).takeIf { it > 0 }

        setupToolbar()
        setupRecyclerView()
        setupComposer()
        renderContextBanner()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshModeHint()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "AI 伴侣记账"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = AiChatAdapter { message ->
            when {
                message.linkedBillId != null -> {
                    startActivity(
                        Intent(this, RecordDetailActivity::class.java).apply {
                            putExtra(RecordDetailActivity.EXTRA_BILL_ID, message.linkedBillId)
                        }
                    )
                }

                message.linkedIncomeId != null -> {
                    startActivity(
                        Intent(this, RecordDetailActivity::class.java).apply {
                            putExtra(RecordDetailActivity.EXTRA_INCOME_ID, message.linkedIncomeId)
                        }
                    )
                }
            }
        }

        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter
    }

    private fun setupComposer() {
        binding.btnSend.setOnClickListener { sendCurrentInput() }
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendCurrentInput()
                true
            } else {
                false
            }
        }
    }

    private fun sendCurrentInput() {
        val text = binding.etMessage.text?.toString().orEmpty().trim()
        if (text.isBlank()) return
        binding.etMessage.setText("")
        viewModel.sendMessage(text)
    }

    private fun renderContextBanner() {
        val linkedBillId = intent.getLongExtra(EXTRA_LINKED_BILL_ID, -1L).takeIf { it > 0 }
        val linkedIncomeId = intent.getLongExtra(EXTRA_LINKED_INCOME_ID, -1L).takeIf { it > 0 }

        binding.tvContextBanner.isVisible = linkedBillId != null || linkedIncomeId != null
        binding.tvContextBanner.text = when {
            linkedBillId != null -> "正在查看这笔支出的聊天上下文"
            linkedIncomeId != null -> "正在查看这笔收入的聊天上下文"
            else -> ""
        }
    }

    private fun observeState() {
        viewModel.messages.observe(this) { messages ->
            binding.tvEmpty.isVisible = messages.isEmpty()
            adapter.submitList(messages) {
                if (!hasScrolledToTarget && targetMessageId != null) {
                    val targetIndex = messages.indexOfFirst { it.id == targetMessageId }
                    if (targetIndex >= 0) {
                        adapter.setHighlightedMessageId(targetMessageId)
                        binding.rvMessages.post { binding.rvMessages.scrollToPosition(targetIndex) }
                        hasScrolledToTarget = true
                        return@submitList
                    }
                }

                if (messages.isNotEmpty()) {
                    binding.rvMessages.post {
                        binding.rvMessages.scrollToPosition(messages.lastIndex)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.modeHint.collect { hint ->
                binding.tvModeHint.text = hint
            }
        }

        lifecycleScope.launch {
            viewModel.isSending.collect { isSending ->
                binding.btnSend.isEnabled = !isSending
                binding.etMessage.isEnabled = !isSending
                binding.progressSending.isVisible = isSending
            }
        }

        viewModel.toastEvent.observe(this) { message ->
            message ?: return@observe
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeToastEvent()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_ai_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_ai_settings -> {
                startActivity(Intent(this, AiSettingsActivity::class.java))
                true
            }

            R.id.action_clear_chat -> {
                AlertDialog.Builder(this)
                    .setTitle("清空聊天")
                    .setMessage("确认清空当前 AI 伴侣聊天记录吗？")
                    .setPositiveButton("清空") { _, _ ->
                        hasScrolledToTarget = true
                        viewModel.clearConversation()
                    }
                    .setNegativeButton("取消", null)
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_TARGET_MESSAGE_ID = "extra_target_message_id"
        const val EXTRA_LINKED_BILL_ID = "extra_linked_bill_id"
        const val EXTRA_LINKED_INCOME_ID = "extra_linked_income_id"
    }
}
