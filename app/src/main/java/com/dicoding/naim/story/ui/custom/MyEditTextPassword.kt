package com.dicoding.naim.story.ui.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.dicoding.naim.story.R

class MyEditTextPassword : AppCompatEditText, View.OnTouchListener {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private var inputValidation: InputValidation? = null

    interface InputValidation {
        val errorMessage: String
        fun validate(input: String): Boolean = input.length >= 6
    }

    fun setValidationCallback(inputValidation: InputValidation) {
        this.inputValidation = inputValidation
    }

    private lateinit var showPasswordButtonImage: Drawable

    private fun init() {
        showPasswordButtonImage = ContextCompat.getDrawable(
            context, R.drawable.ic_baseline_remove_red_eye_24
        ) as Drawable

        setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            showPasswordButtonImage,
            null
        )

        setOnTouchListener(this)

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // nothing
            }

            override fun afterTextChanged(s: Editable?) {
                validateInput()
            }
        })
    }

    fun validateInput(): Boolean {
        val input = text.toString()
        val isValid = inputValidation?.validate(input) ?: true

        error = if (isValid) {
            null
        } else {
            inputValidation?.errorMessage ?: ""
        }

        return isValid
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        var isButtonTouched = false

        if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            val buttonEnd = (showPasswordButtonImage.intrinsicWidth + paddingStart)
                .toFloat()

            if (event.x < buttonEnd) {
                isButtonTouched = true
            }
        } else {
            val buttonStart = (width - paddingEnd - showPasswordButtonImage.intrinsicWidth)
                .toFloat()

            if (event.x > buttonStart) {
                isButtonTouched = true
            }
        }

        if (isButtonTouched) {
            val curSelection = selectionEnd

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    transformationMethod = HideReturnsTransformationMethod.getInstance()
                    setSelection(curSelection)
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    transformationMethod = PasswordTransformationMethod.getInstance()
                    setSelection(curSelection)
                    return true
                }
            }
        }

        return false
    }

}
