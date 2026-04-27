package com.example.budget.ui

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.NumberFormat
import java.util.*

class MoneyTextWatcher(private val editText: EditText) : TextWatcher {
    private var current = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s.toString() != current) {
            editText.removeTextChangedListener(this)

            val cleanString = s.toString().replace("[$,.]".toRegex(), "")
            if (cleanString.isNotEmpty()) {
                val parsed = cleanString.toDouble()
                val formatted = NumberFormat.getCurrencyInstance(Locale.US).format(parsed / 100)
                current = formatted
                editText.setText(formatted)
                editText.setSelection(formatted.length)
            } else {
                current = ""
                editText.setText("")
            }

            editText.addTextChangedListener(this)
        }
    }

    override fun afterTextChanged(s: Editable?) {}
}
