package com.wbrawner.budget.ui.transaction

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wbrawner.budget.ui.base.TwigsApp
import com.wbrawner.budget.ui.util.DatePicker
import com.wbrawner.twigs.shared.Store
import com.wbrawner.twigs.shared.category.Category
import com.wbrawner.twigs.shared.transaction.Transaction
import com.wbrawner.twigs.shared.transaction.TransactionAction
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TransactionFormDialog(store: Store) {
    Dialog(
        onDismissRequest = { store.dispatch(TransactionAction.CancelEditTransaction) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        TransactionForm(store)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionForm(store: Store) {
    val state by store.state.collectAsState()
    val transaction = remember {
        val defaultTransaction = Transaction(
            title = "",
            date = Clock.System.now(),
            amount = 0L,
            budgetId = state.selectedBudget!!,
            categoryId = state.selectedCategory,
            expense = true,
            createdBy = state.user!!.id!!
        )
        if (state.selectedTransaction.isNullOrBlank()) {
            defaultTransaction
        } else {
            state.transactions?.first { it.id == state.selectedTransaction } ?: defaultTransaction
        }
    }
    val (title, setTitle) = remember { mutableStateOf(transaction.title) }
    val (description, setDescription) = remember { mutableStateOf(transaction.description ?: "") }
    val (date, setDate) = remember { mutableStateOf(transaction.date) }
    val (amount, setAmount) = remember { mutableStateOf(transaction.amount.toDecimalString()) }
    val (expense, setExpense) = remember { mutableStateOf(transaction.expense) }
    val budget = remember { state.budgets!!.first { it.id == transaction.budgetId } }
    val (category, setCategory) = remember { mutableStateOf(transaction.categoryId?.let { categoryId -> state.categories?.firstOrNull { it.id == categoryId } }) }
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { store.dispatch(TransactionAction.CancelEditTransaction) }) {
                        Icon(Icons.Default.Close, "Cancel")
                    }
                },
                title = {
                    Text(if (transaction.id.isNullOrBlank()) "New Transaction" else "Edit Transaction")
                }
            )
        }
    ) {
        TransactionForm(
            modifier = Modifier.padding(it),
            title = title,
            setTitle = setTitle,
            description = description,
            setDescription = setDescription,
            date = date,
            setDate = setDate,
            amount = amount,
            setAmount = setAmount,
            expense = expense,
            setExpense = setExpense,
            categories = state.categories?.filter { c -> c.expense == expense } ?: emptyList(),
            category = category,
            setCategory = setCategory
        ) {
            store.dispatch(
                transaction.id?.let { id ->
                    TransactionAction.UpdateTransaction(
                        id = id,
                        title = title,
                        amount = (amount.toDouble() * 100).toLong(),
                        date = date,
                        expense = expense,
                        category = category,
                        budget = budget
                    )
                } ?: TransactionAction.CreateTransaction(
                    title = title,
                    description = description,
                    amount = (amount.toDouble() * 100).toLong(),
                    date = date,
                    expense = expense,
                    category = category,
                    budget = budget
                )
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionForm(
    modifier: Modifier,
    title: String,
    setTitle: (String) -> Unit,
    description: String,
    setDescription: (String) -> Unit,
    date: Instant,
    setDate: (Instant) -> Unit,
    amount: String,
    setAmount: (String) -> Unit,
    expense: Boolean,
    setExpense: (Boolean) -> Unit,
    categories: List<Category>,
    category: Category?,
    setCategory: (Category?) -> Unit,
    save: () -> Unit
) {
    val scrollState = rememberScrollState()
    val (titleInput, descriptionInput, amountInput, dateInput) = FocusRequester.createRefs()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
//        if (error.isNotBlank()) {
//            Text(text = error, color = Color.Red)
//        }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(titleInput)
                .onPreviewKeyEvent {
                    if (it.key == Key.Tab && !it.isShiftPressed) {
                        descriptionInput.requestFocus()
                        true
                    } else {
                        false
                    }
                },
            value = title,
            onValueChange = setTitle,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            label = { Text("Title") },
            keyboardActions = KeyboardActions(onNext = {
                descriptionInput.requestFocus()
            }),
            maxLines = 1
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(descriptionInput)
                .onPreviewKeyEvent {
                    if (it.key == Key.Tab) {
                        if (it.isShiftPressed) {
                            titleInput.requestFocus()
                        } else {
                            amountInput.requestFocus()
                        }
                        true
                    } else {
                        false
                    }
                },
            value = description,
            onValueChange = setDescription,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            label = { Text("Description") },
            keyboardActions = KeyboardActions(onNext = {
                amountInput.requestFocus()
            }),
        )
        val keyboardController = LocalSoftwareKeyboardController.current
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(amountInput)
                .onPreviewKeyEvent {
                    if (it.key == Key.Tab && it.isShiftPressed) {
                        descriptionInput.requestFocus()
                        true
                    } else {
                        false
                    }
                },
            value = amount,
            onValueChange = setAmount,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            label = { Text("Amount") },
            keyboardActions = KeyboardActions(onNext = {
                keyboardController?.hide()
            }),
        )
        val (datePickerVisible, setDatePickerVisible) = remember { mutableStateOf(false) }
        DatePicker(
            modifier = Modifier.fillMaxWidth(),
            date = date,
            setDate = setDate,
            dialogVisible = datePickerVisible,
            setDialogVisible = setDatePickerVisible
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = spacedBy(8.dp)) {
            Row(
                modifier = Modifier.clickable {
                    setExpense(true)
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = expense, onClick = { setExpense(true) })
                Text(text = "Expense")
            }
            Row(
                modifier = Modifier.clickable {
                    setExpense(false)
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = !expense, onClick = { setExpense(false) })
                Text(text = "Income")
            }
        }
        val (categoriesExpanded, setCategoriesExpanded) = remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            modifier = Modifier
                .fillMaxWidth(),
            expanded = categoriesExpanded,
            onExpandedChange = setCategoriesExpanded,
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = category?.title ?: "",
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Category")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriesExpanded)
                }
            )
            ExposedDropdownMenu(expanded = categoriesExpanded, onDismissRequest = {
                setCategoriesExpanded(false)
            }) {
                categories.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c.title) },
                        onClick = {
                            setCategory(c)
                            setCategoriesExpanded(false)
                        }
                    )
                }
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = save
        ) {
            Text("Save")
        }
    }
}

@Composable
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
fun TransactionForm_Preview() {
    TwigsApp {
        TransactionForm(store = Store(reducers = emptyList()))
    }
}