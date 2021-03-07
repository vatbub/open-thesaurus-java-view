package com.github.vatbub.openthesaurus.preferences

import com.github.vatbub.kotlin.preferences.Preferences
import com.github.vatbub.kotlin.preferences.PropertiesFileKeyValueProvider
import com.github.vatbub.openthesaurus.util.appDataFolder
import java.io.File

val preferences by lazy {
    Preferences(PropertiesFileKeyValueProvider(File(appDataFolder, "settings.properties")))
}
