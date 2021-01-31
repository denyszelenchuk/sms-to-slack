package com.slacksms.app.donate

import android.text.InputFilter
import android.text.Spanned

class InputFilterMinMax(min: String, max: String) : InputFilter {
    private var min: Int = min.toInt()
    private var max: Int = max.toInt()

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toInt()
            if (isInRange(min, max, input)) return null
        } catch (nfe: NumberFormatException) {
        }
        return ""
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}
