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
package com.github.vatbub.openthesaurus.apiclient

import com.github.vatbub.openthesaurus.apiclient.bighugethesaurus.BigHugeThesaurusProvider
import com.github.vatbub.openthesaurus.apiclient.duden.DudenProvider
import com.github.vatbub.openthesaurus.apiclient.openthesaurus.OpenThesaurusProvider
import com.github.vatbub.openthesaurus.util.Either
import java.util.Locale

interface DataProvider {
    /**
     * The name to be shown in the settings UI.
     */
    val screenName: String

    /**
     * An internal name to store the provider selection in the preferences. Must be unique.
     */
    val internalName: String

    /**
     * Languages supported by the provider.
     */
    val supportedLocales: List<Locale>

    /**
     * Callback to performa a search. Note that the callback should not perform any caching, as caching is already done in
     * [cacheResults].
     * @param term The word to get synonyms for
     * @param searchLocale The [Locale] to perform the search in. If the locale is not in [supportedLocales],
     * an [IllegalArgumentException] may be thrown by the implementation.
     */
    suspend fun requestTerm(term: String, searchLocale: Locale): Either<Response, ApiError>

    companion object {
        val knownImplementations: List<DataProvider> by lazy {
            listOf(
                OpenThesaurusProvider().cacheResults(),
                BigHugeThesaurusProvider().cacheResults(),
                DudenProvider().cacheResults()
            )
        }
    }
}
