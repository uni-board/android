package com.uniboard.board_details.presentation.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun createSnackbar(view: View, text: CharSequence, color: Int) {
    Snackbar.make(
        view,
        text,
        Snackbar.LENGTH_SHORT
    ).setTextColor(color).show()
}