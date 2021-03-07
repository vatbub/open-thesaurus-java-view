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
package com.github.vatbub.openthesaurus.apiclient.openthesaurus

import com.github.vatbub.openthesaurus.apiclient.*
import com.github.vatbub.openthesaurus.util.Either
import com.github.vatbub.openthesaurus.util.left
import com.github.vatbub.openthesaurus.util.right
import java.io.Closeable
import java.util.*

class OpenThesaurusProvider(
    endpoint: String = "https://www.openthesaurus.de/",
    cacheSize: Int = 50
) : DataProvider, Closeable {
    override val screenName: String = "OpenThesaurus.de"
    override val supportedLocales: List<Locale> = listOf(Locale.GERMAN)

    private val client by lazy { OpenThesaurusClient(endpoint, cacheSize) }

    override suspend fun requestTerm(term: String, searchLocale:Locale): Either<Response, ApiError> {
        if (searchLocale !in supportedLocales) throw IllegalArgumentException("Locale $searchLocale not supported")
        return client.request(
            OpenThesaurusRequest(
                term,
                superSynonymSets = false,
                subSynonymSets = false
            )
        ).leftOr { return it.right() }
            .toResponse().left()
    }

    private fun OpenThesaurusResult.toResponse(): Response = Response.dynamicResponse(
        synonyms = this.synonymSets.toResultTermList(),
        similarTerms = this.similarTerms?.toSimilarTermList(),
        substringTerms = this.substringTerms?.toResultTermList(),
        baseForms = this.baseForms?.toResultTermList()
    )

    private fun List<OpenThesaurusSynonymSet>.toResultTermList(): List<ResultTerm> =
        this.map { it.terms }
            .flatten()
            .map { ResultTermImpl(it.term, it.level) }

    @JvmName("toResultTermListOpenThesaurusTerm")
    private fun List<OpenThesaurusTerm>.toResultTermList(): List<ResultTerm> =
        this.map { ResultTermImpl(it.term, it.level) }

    private fun List<OpenThesaurusTerm>.toSimilarTermList(): List<SimilarResultTerm> =
        this.map { SimilarResultTermImpl(it.term, it.distance!!, it.level) }

    override fun close() {
        client.close()
    }
}
