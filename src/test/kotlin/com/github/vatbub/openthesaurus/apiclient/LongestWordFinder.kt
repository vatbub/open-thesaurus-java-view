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
package com.github.vatbub.openthesaurus.apiclient

import com.github.vatbub.openthesaurus.apiclient.openthesaurus.OfflineOpenThesaurusProvider
import com.github.vatbub.openthesaurus.apiclient.openthesaurus.OpenThesaurusDatabaseCache
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class LongestWordFinder {
    @Test
    @Disabled
    fun findLongestWord() {
        OpenThesaurusDatabaseCache.downloadIfNecessary()
        val longestWord = OfflineOpenThesaurusProvider().iterateOverOfflineDatabase { sequence ->
            sequence.flatten()
                .flatMap { it.term.split(" ", "-") }
                .maxByOrNull { it.length }
        }.leftOr { error("API error occurred") }

        if (longestWord == null) error("Database empty?")
        println(longestWord)
    }

    @Test
    @Disabled
    fun numberOfUniqueTerms() {
        OpenThesaurusDatabaseCache.downloadIfNecessary()
        val numberOfUniqueTerms = OfflineOpenThesaurusProvider().iterateOverOfflineDatabase { sequence ->
            sequence.flatten()
                .count()
        }.leftOr { error("API error occurred") }

        println(numberOfUniqueTerms)
    }
}
