package com.wbrawner.myallowance.addedittransaction

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.wbrawner.myallowance.R
import com.wbrawner.myallowance.data.Transaction
import com.wbrawner.myallowance.data.TransactionType
import com.wbrawner.myallowance.transactions.TransactionViewModel
import kotlinx.android.synthetic.main.activity_add_edit_transaction.*
import java.util.*

class AddEditTransactionActivity : AppCompatActivity() {
    lateinit var viewModel: TransactionViewModel
    lateinit var type: TransactionType
    var id: Int? = null
    var date: Date = Date()
    var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_transaction)
        setSupportActionBar(action_bar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel = ViewModelProviders.of(this).get(TransactionViewModel::class.java)
        if (intent?.hasExtra(EXTRA_TYPE) == true) {
            type = TransactionType.valueOf(intent?.extras?.getString(EXTRA_TYPE, "EXPENSE")
                    ?: "EXPENSE")
            setTitle(type.addTitle)
            return
        } else if (intent?.hasExtra(EXTRA_TRANSACTION_ID) != true) {
            finish()
            return
        }

        viewModel.getTransaction(intent!!.extras!!.getInt(EXTRA_TRANSACTION_ID))
                .observe(this, Observer<Transaction> { transaction ->
                    if (transaction == null) {
                        menu?.findItem(R.id.action_delete)?.isVisible = false
                        return@Observer
                    }
                    id = transaction.id
                    type = transaction.type
                    setTitle(type.editTitle)
                    menu?.findItem(R.id.action_delete)?.isVisible = true
                    edit_transaction_title.setText(transaction.title)
                    edit_transaction_description.setText(transaction.description)
                    edit_transaction_amount.setText(String.format("%.02f", transaction.amount))
                    val field = Calendar.getInstance()
                    field.time = transaction.date
                    val year = field.get(Calendar.YEAR)
                    val month = field.get(Calendar.MONTH)
                    val day = field.get(Calendar.DAY_OF_MONTH)
                    edit_transaction_date.updateDate(year, month, day)
                })
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_edit, menu)
        if (id != null) {
            menu?.findItem(R.id.action_delete)?.isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_save -> {
                val field = edit_transaction_date
                val cal = Calendar.getInstance()
                cal.set(field.year, field.month, field.dayOfMonth)

                viewModel.saveTransaction(Transaction(
                        id = id,
                        title = edit_transaction_title.text.toString(),
                        date = cal.time,
                        description = edit_transaction_description.text.toString(),
                        amount = edit_transaction_amount.text.toString().toDouble(),
                        type = type
                ))
                finish()
            }
            R.id.action_delete -> {
                viewModel.deleteTransactionById(this@AddEditTransactionActivity.id!!)
                finish()
            }
        }
        return true
    }


    companion object {
        const val EXTRA_TYPE = "EXTRA_TRANSACTION_TYPE"
        const val EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID"
    }
}
