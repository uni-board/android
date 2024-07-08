package com.uniboard.board_details.presentation.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun copyToClipboard(context: Context, text: CharSequence?) {
    var clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var clip = ClipData.newPlainText("text", text)
    clipboard.setPrimaryClip(clip)
}

