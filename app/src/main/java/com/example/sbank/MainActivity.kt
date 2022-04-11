package com.example.sbank

import android.os.Bundle
import android.text.*
import android.text.InputFilter.AllCaps
import android.text.InputFilter.LengthFilter
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sbank.databinding.ActivityMainBinding
import com.example.sbank.utils.EnumClasses
import com.example.sbank.utils.EnumClasses.ButtonState.ENABLE
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val model: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUp()
        setUpCollectors()
        setUpListeners()
        setUpClick()
        setUpTnC()
    }

    //To show learn more in different color
    private fun setUpTnC() {
        binding.tvTnc.makeLinks(
            Pair(getString(R.string.learn_more), View.OnClickListener {
            })
        )
    }

    //All click will be set up here
    private fun setUpClick() {
        binding.btnNext.setOnClickListener {
            nextButtonClick()
        }
        binding.tvNoPan.setOnClickListener {
            finish()
        }
    }

    //When next button clicked
    private fun nextButtonClick() {
        Toast.makeText(this@MainActivity, getString(R.string.details_submitted), Toast.LENGTH_SHORT)
            .show()
        finish()
    }

    //Set up anything needed
    private fun setUp() {
        //limit pan number to its length and convert alphabet to capital
        binding.tiePan.filters = arrayOf(LengthFilter(AppConstants.PAN_LENGTH), AllCaps())
    }

    //all text input will be listened here
    private fun setUpListeners() {
        binding.tiePan.let {
            it.doOnTextChanged { text, _, _, _ ->
                model.setData(text.toString(), EnumClasses.DataType.PAN)
            }
        }
        binding.tieDay.let {
            it.doOnTextChanged { text, _, _, _ ->
                model.setData(text.toString(), EnumClasses.DataType.DAY)
            }
        }
        binding.tieMonth.let {
            it.doOnTextChanged { text, _, _, _ ->
                model.setData(text.toString(), EnumClasses.DataType.MONTH)
            }
        }
        binding.tieYear.let {
            it.doOnTextChanged { text, _, _, _ ->
                model.setData(text.toString(), EnumClasses.DataType.YEAR)
            }
        }
    }

    //To collect all flows
    private fun setUpCollectors() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    //To enable / disable the button
                    model.nextButtonState.collect { buttonState ->
                        when (buttonState) {
                            ENABLE ->
                                binding.btnNext.isEnabled = true
                            else ->
                                binding.btnNext.isEnabled = false
                        }
                    }
                }

                launch {
                    //To change the cursor focus
                    model.focusState.collect { focusState ->
                        when (focusState) {
                            EnumClasses.FocusState.FOCUS_DAY -> {
                                binding.tieDay.run {
                                    requestFocus()
                                    setSelection(length())
                                }
                            }
                            EnumClasses.FocusState.FOCUS_MONTH -> {
                                binding.tieMonth.run {
                                    requestFocus()
                                    setSelection(length())
                                }
                            }
                            EnumClasses.FocusState.FOCUS_YEAR -> {
                                binding.tieYear.run {
                                    requestFocus()
                                    setSelection(length())
                                }
                            }
                        }
                    }
                }
                launch {
                    //On activity recreated data will be re populated
                    model.userDataState.collectLatest { data ->
                        binding.tiePan.run {
                            if (binding.tiePan.text.toString() != data.panNumber) {
                                setText(data.panNumber)
                                setSelection(length())
                            }
                        }
                        binding.tieDay.run {
                            if (binding.tieDay.text.toString() != data.day) {
                                setText(data.day)
                                setSelection(length())
                            }
                        }

                        binding.tieMonth.run {
                            if (binding.tieMonth.text.toString() != data.month) {
                                setText(data.month)
                                setSelection(length())
                            }
                        }
                        binding.tieYear.run {
                            if (binding.tieYear.text.toString() != data.year) {
                                setText(data.year)
                                setSelection(length())
                            }
                        }
                    }
                }

            }
        }
    }

    //To make part of the TnC in different color
    private fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
        val spannableString = SpannableString(this.text)
        var startIndexOfLink = -1
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                override fun updateDrawState(textPaint: TextPaint) {
                    textPaint.run {
                        color = linkColor
                        isUnderlineText = false
                    }
                }

                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
            spannableString.setSpan(
                clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        this.movementMethod =
            LinkMovementMethod.getInstance()
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }
}