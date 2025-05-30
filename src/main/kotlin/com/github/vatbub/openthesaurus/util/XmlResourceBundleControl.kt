/*-
 * #%L
 * Open Thesaurus Java View
 * %%
 * Copyright (C) 2019 - 2025 Frederik Kammel
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
package com.github.vatbub.openthesaurus.util

import java.io.InputStream
import java.util.Collections
import java.util.Enumeration
import java.util.Locale
import java.util.Properties
import java.util.ResourceBundle


object XmlResourceBundleControl : ResourceBundle.Control() {
    override fun getFormats(baseName: String): List<String> {
        return listOf("xml")
    }

    override fun newBundle(
        baseName: String,
        locale: Locale,
        format: String,
        loader: ClassLoader,
        reload: Boolean
    ): ResourceBundle? {
        if (!format.equals("xml", ignoreCase = true)) return null
        val bundleName = toBundleName(baseName, locale)
        val resourceName = toResourceName(bundleName, format)
        val stream = if (reload) {
            val url = loader.getResource(resourceName) ?: return null
            val connection = url.openConnection() ?: return null
            // Disable caches to get fresh data for
            // reloading.
            connection.setUseCaches(false)
            connection.getInputStream()
        } else {
            loader.getResourceAsStream(resourceName)
        }

        stream.use { stream ->
            stream.buffered().use { bufferedInputStream ->
                return XmlResourceBundle(bufferedInputStream)
            }
        }
    }
}

class XmlResourceBundle(stream: InputStream) : ResourceBundle() {
    private val properties = Properties()

    init {
        properties.loadFromXML(stream)
    }

    override fun handleGetObject(key: String): Any? = properties.getProperty(key)

    override fun getKeys(): Enumeration<String?> {
        val parentKeys = parent?.keys?.toList().orEmpty()
        val resultingKeys = properties.keys.map { it.toString() } + parentKeys
        return Collections.enumeration(resultingKeys)
    }
}
