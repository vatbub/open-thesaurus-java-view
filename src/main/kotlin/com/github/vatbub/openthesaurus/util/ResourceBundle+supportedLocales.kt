package com.github.vatbub.openthesaurus.util

import java.text.DateFormat
import java.util.*

val ResourceBundle.supportedLocales: List<Locale>
    get() = DateFormat.getAvailableLocales()
        .filter {
            try {
                val newBundle = ResourceBundle.getBundle(baseBundleName, it)
                newBundle.locale == it
            } catch (_: MissingResourceException) {
                false
            }
        }
