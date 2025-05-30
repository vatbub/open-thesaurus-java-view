/*-
 * #%L
 * Open Thesaurus Java View
 * %%
 * Copyright (C) 2016 - 2021 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.openthesaurus.preferences

import com.github.vatbub.kotlin.preferences.Key
import com.github.vatbub.openthesaurus.apiclient.DataProvider
import java.util.Locale

object PreferenceKeys {
    object GuiLanguage : LocaleKey("guiLanguage")

    object DataSource : Key<DataProvider>(
        uniqueName = "dataSource",
        defaultValue = DataProvider.knownImplementations.first(),
        parser = { string ->
            DataProvider.knownImplementations.first { implementation ->
                implementation.internalName == string
            }
        },
        serializer = { it.internalName })

    object SearchLanguage : LocaleKey("searchLanguage")

    object AutoSearchFromClipboard :
        Key<Boolean>("autoSearchFromClipboard", true, { it.toBoolean() }, { it.toString() })

    object FilterAutoSendFromClipboard :
        Key<Boolean>("filterAutoSendFromClipboard", true, { it.toBoolean() }, { it.toString() })

    object BigHugeThesaurusApiKey :
        Key<String>("bigHugeThesaurusApiKey", "", { it }, { it })
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
