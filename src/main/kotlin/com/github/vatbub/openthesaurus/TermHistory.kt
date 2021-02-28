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
package com.github.vatbub.openthesaurus

import javafx.beans.property.*

class TermHistory(private val maxHistoryLength: Int = 50) {
    private val history = mutableListOf<String>()
    private var positionInHistoryProperty: IntegerProperty = SimpleIntegerProperty(-1)

    private val internalCanGoBackProperty: BooleanProperty = SimpleBooleanProperty(false)

    val canGoBackProperty: ReadOnlyBooleanProperty = internalCanGoBackProperty

    private val internalCanGoForwardProperty: BooleanProperty = SimpleBooleanProperty(false)

    val canGoForwardProperty: ReadOnlyBooleanProperty = internalCanGoForwardProperty

    init {
        positionInHistoryProperty.addListener { _, _, newValue ->
            updateCanProperties(newValue.toInt())
        }
    }

    private fun updateCanProperties(positionInHistory: Int) {
        internalCanGoBackProperty.set(positionInHistory < history.size - 1)
        internalCanGoForwardProperty.set(positionInHistory > 0)
    }

    fun goBack(): String {
        positionInHistoryProperty.set(positionInHistoryProperty.get() + 1)
        return history[positionInHistoryProperty.get()]
    }

    fun goForward(): String {
        positionInHistoryProperty.set(positionInHistoryProperty.get() - 1)
        return history[positionInHistoryProperty.get()]
    }

    fun appendToHistory(term: String) {
        if (canGoForwardProperty.get() && goForward() == term)
            return

        positionInHistoryProperty.set(0)

        // Avoid duplicate entries in a row
        if (history.isNotEmpty() && history[0] == term) return

        history.add(0, term)
        while (history.size > maxHistoryLength)
            history.removeLast()
        updateCanProperties(positionInHistoryProperty.get())
    }
}
