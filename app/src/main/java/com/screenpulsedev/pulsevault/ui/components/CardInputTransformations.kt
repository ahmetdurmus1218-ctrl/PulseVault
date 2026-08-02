package com.screenpulsedev.pulsevault.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Groups raw digits into "1234 5678 ..." for DISPLAY only — the underlying
 * TextField state stays as plain digits. Using OffsetMapping (instead of storing
 * the formatted string as the actual value) is what keeps the cursor landing in
 * the right spot while typing/deleting in the middle, not just at the end.
 */
class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(19)
        val formatted = buildString {
            digits.forEachIndexed { index, c ->
                if (index != 0 && index % 4 == 0) append(' ')
                append(c)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                offset + (offset - 1).coerceAtLeast(0) / 4

            override fun transformedToOriginal(offset: Int): Int =
                offset - (offset - 1).coerceAtLeast(0) / 5
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

/** "1226" -> "12/26" for display, same cursor-safe approach. */
class ExpiryVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(4)
        val formatted = if (digits.length <= 2) digits else "${digits.take(2)}/${digits.drop(2)}"
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset <= 2) offset else offset + 1

            override fun transformedToOriginal(offset: Int): Int =
                if (offset <= 2) offset else offset - 1
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
