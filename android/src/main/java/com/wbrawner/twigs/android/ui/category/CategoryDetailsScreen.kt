package com.wbrawner.twigs.android.ui.category

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wbrawner.twigs.android.ui.TwigsScaffold
import com.wbrawner.twigs.android.ui.base.TwigsApp
import com.wbrawner.twigs.android.ui.transaction.TransactionFormDialog
import com.wbrawner.twigs.android.ui.transaction.TransactionListItem
import com.wbrawner.twigs.android.ui.transaction.toCurrencyString
import com.wbrawner.twigs.android.ui.util.format
import com.wbrawner.twigs.shared.Action
import com.wbrawner.twigs.shared.Store
import com.wbrawner.twigs.shared.category.Category
import com.wbrawner.twigs.shared.category.CategoryAction
import com.wbrawner.twigs.shared.transaction.Transaction
import com.wbrawner.twigs.shared.transaction.TransactionAction
import com.wbrawner.twigs.shared.transaction.groupByDate
import kotlinx.datetime.Clock
import kotlinx.datetime.toInstant
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailsScreen(store: Store) {
    val state by store.state.collectAsState()
    val category =
        remember(state.editingCategory) { state.categories!!.first { it.id == state.selectedCategory } }
    TwigsScaffold(
        store = store,
        title = category.title,
        navigationIcon = {
            IconButton(onClick = { store.dispatch(Action.Back) }) {
                Icon(Icons.Default.ArrowBack, "Go back")
            }
        },
        actions = {
            IconButton({ store.dispatch(CategoryAction.EditCategory(requireNotNull(category.id))) }) {
                Icon(Icons.Default.Edit, "Edit")
            }
        },
        onClickFab = {
            store.dispatch(TransactionAction.NewTransactionClicked)
        }
    ) { padding ->
        CategoryDetails(
            modifier = Modifier.padding(padding),
            category = category,
            balance = state.categoryBalances!![category.id!!] ?: 0,
            transactions = state.transactions!!.filter { it.categoryId == category.id },
            onTransactionClicked = { store.dispatch(TransactionAction.SelectTransaction(it.id)) }
        )
        if (state.editingTransaction) {
            TransactionFormDialog(store = store)
        }
        if (state.editingCategory) {
            CategoryFormDialog(store = store)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryDetails(
    modifier: Modifier = Modifier,
    category: Category,
    balance: Long,
    transactions: List<Transaction>,
    onTransactionClicked: (Transaction) -> Unit
) {
    val transactionGroups = remember { transactions.groupByDate() }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        category.description?.let {
            item {
                Text(modifier = Modifier.padding(8.dp), text = it)
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LabeledCounter("Planned", category.amount.toCurrencyString())
                LabeledCounter("Actual", abs(balance).toCurrencyString())
                LabeledCounter("Remaining", (category.amount - abs(balance)).toCurrencyString())
            }
        }
        transactionGroups.forEach { (timestamp, transactions) ->
            item(timestamp) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = timestamp.toInstant().format(LocalContext.current),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            itemsIndexed(transactions) { index, transaction ->
                TransactionListItem(
                    modifier = Modifier.animateItemPlacement(),
                    transaction = transaction,
                    isFirst = index == 0,
                    isLast = index == transactions.lastIndex,
                    onClick = onTransactionClicked
                )
            }
        }
    }
}

@Composable
fun LabeledCounter(label: String, counter: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = counter, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
fun CategoryDetails_Preview() {
    TwigsApp {
        CategoryDetails(
            category = Category(title = "Coffee", budgetId = "budget", amount = 1000),
            balance = 500,
            transactions = listOf(
                Transaction(
                    title = "DAZBOG",
                    description = "Chokolat Cappuccino",
                    date = Clock.System.now(),
                    amount = 550,
                    categoryId = "coffee",
                    budgetId = "budget",
                    createdBy = "user",
                    expense = true
                )
            ),
            onTransactionClicked = {}
        )
    }
}