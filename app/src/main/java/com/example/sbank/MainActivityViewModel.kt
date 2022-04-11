package com.example.sbank

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sbank.AppConstants.DAY_LENGTH
import com.example.sbank.AppConstants.MIN_YEAR
import com.example.sbank.AppConstants.MONTH_LENGTH
import com.example.sbank.AppConstants.PAN_LENGTH
import com.example.sbank.AppConstants.YEAR_LENGTH
import com.example.sbank.AppConstants.ddMMyyyy
import com.example.sbank.module.UserData
import com.example.sbank.utils.EnumClasses
import com.example.sbank.utils.EnumClasses.ButtonState.DISABLE
import com.example.sbank.utils.EnumClasses.ButtonState.ENABLE
import com.example.sbank.utils.EnumClasses.FocusState
import com.example.sbank.utils.EnumClasses.FocusState.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class MainActivityViewModel : ViewModel() {

    //To hold focus state
    private val _focusState = MutableSharedFlow<FocusState>()
    val focusState = _focusState.asSharedFlow()

    //To hold next button state
    private val _nextButtonState = MutableStateFlow(DISABLE)
    val nextButtonState = _nextButtonState.asStateFlow()

    //To hold user data
    private val _userDataState = MutableStateFlow(UserData())
    val userDataState = _userDataState.asStateFlow()


    //Regular expression for validating pan number
    private fun validatePanNumber(): Boolean {
        return _userDataState.value.panNumber.let { panNumber ->
            val pattern: Pattern =
                Pattern.compile("[A-Z]{3}[ABCFGHLJPT][A-Z][0-9]{4}[A-Z]")
            val matcher: Matcher = pattern.matcher(panNumber)
            matcher.matches()
        }
    }

    //data will be hold in flow
    fun setData(data: String, dataType: EnumClasses.DataType) {
        viewModelScope.launch {
            when (dataType) {
                EnumClasses.DataType.PAN -> {
                    _userDataState.value.panNumber = data

                    if (data.length == PAN_LENGTH && _userDataState.value.day.length != DAY_LENGTH) {
                        _focusState.emit(FOCUS_DAY)
                    }
                }
                EnumClasses.DataType.DAY -> {
                    _userDataState.value.day = data
                    if (data.length == DAY_LENGTH && _userDataState.value.month.length != MONTH_LENGTH) {
                        _focusState.emit(FOCUS_MONTH)
                    }
                }
                EnumClasses.DataType.MONTH -> {
                    _userDataState.value.month = data
                    if (data.length == MONTH_LENGTH && _userDataState.value.year.length != YEAR_LENGTH) {
                        _focusState.emit(FOCUS_YEAR)
                    }
                }
                EnumClasses.DataType.YEAR -> {
                    _userDataState.value.year = data
                }

            }
        }
        if (_userDataState.value.panNumber.length == PAN_LENGTH && _userDataState.value.day.length == DAY_LENGTH && _userDataState.value.month.length == MONTH_LENGTH && _userDataState.value.year.length == YEAR_LENGTH) {
            if (validatePanNumber() && validateDate(_userDataState.value.day + _userDataState.value.month + _userDataState.value.year) && (MIN_YEAR < Integer.valueOf(
                    _userDataState.value.year
                ) && Integer.valueOf(
                    _userDataState.value.year
                ) <= getCurrentYear())
            ) {
                _nextButtonState.value = ENABLE
            }
        } else {
            _nextButtonState.value = DISABLE
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun validateDate(dateStr: String): Boolean {
        val dateFormat = SimpleDateFormat(ddMMyyyy)
        dateFormat.isLenient = false
        try {
            dateFormat.parse(dateStr.trim())
        } catch (pe: ParseException) {
            return false
        }
        return true
    }

    //To get current year (max year valid is current year)
    private fun getCurrentYear() = Calendar.getInstance().get(Calendar.YEAR)
}

