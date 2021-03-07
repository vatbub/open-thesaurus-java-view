package com.github.vatbub.openthesaurus.preferences

import com.github.vatbub.kotlin.preferences.Key
import com.github.vatbub.openthesaurus.apiclient.DataProvider
import java.util.*

object PreferenceKeys {
    object GuiLanguage : LocaleKey("guiLanguage")

    object DataSource : Key<DataProvider>(
        uniqueName = "dataSource",
        defaultValue = DataProvider.knownImplementations.first(),
        parser = { string ->
            DataProvider.knownImplementations.first { implementation ->
                implementation::class.java.canonicalName == string
            }
        },
        serializer = { it::class.java.canonicalName })

    object SearchLanguage : LocaleKey("searchLanguage")

    object AutoSearchFromClipboard :
        Key<Boolean>("autoSearchFromClipboard", true, { it.toBoolean() }, { it.toString() })

    object FilterAutoSendFromClipboard :
        Key<Boolean>("filterAutoSendFromClipboard", true, { it.toBoolean() }, { it.toString() })
}

abstract class LocaleKey(uniqueName: String, defaultValue: Locale = Locale.getDefault()) :
    Key<Locale>(
        uniqueName = uniqueName,
        defaultValue = defaultValue,
        parser = {
            val parts = it.split(";")
            Locale(parts[0], parts[1], parts[2])
        },
        serializer = { "${it.language};${it.country};${it.variant}" }
    )
