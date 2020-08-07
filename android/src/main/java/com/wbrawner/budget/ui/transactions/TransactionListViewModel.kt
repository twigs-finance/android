package com.wbrawner.budget.ui.transactions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wbrawner.budget.AsyncState
import com.wbrawner.budget.AsyncViewModel
import com.wbrawner.budget.common.transaction.Transaction
import com.wbrawner.budget.common.transaction.TransactionRepository
import com.wbrawner.budget.launch
import java.util.*
import javax.inject.Inject

class TransactionListViewModel : ViewModel(), AsyncViewModel<List<Transaction>> {
    @Inject lateinit var transactionRepo: TransactionRepository
    override val state: MutableLiveData<AsyncState<List<Transaction>>> = MutableLiveData(AsyncState.Loading)

    fun getTransactions(
            budgetId: Long? = null,
            categoryId: Long? = null,
            start: Calendar? = null,
            end: Calendar? = null
    ) {
        val budgets = budgetId?.let { listOf(it) }
        val categories = categoryId?.let { listOf(it) }
        launch {
            transactionRepo.findAll(budgets, categories, start, end).toList()
        }
    }
}