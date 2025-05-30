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
package com.github.vatbub.openthesaurus.apiclient.bighugethesaurus

import com.github.vatbub.openthesaurus.apiclient.ApiError
import com.github.vatbub.openthesaurus.apiclient.DataProvider
import com.github.vatbub.openthesaurus.apiclient.Response
import com.github.vatbub.openthesaurus.apiclient.ResponseImpl
import com.github.vatbub.openthesaurus.apiclient.ResponseWithAntonyms
import com.github.vatbub.openthesaurus.apiclient.ResponseWithAntonymsImpl
import com.github.vatbub.openthesaurus.apiclient.ResponseWithSimilarTerms
import com.github.vatbub.openthesaurus.apiclient.ResponseWithSimilarTermsImpl
import com.github.vatbub.openthesaurus.apiclient.ResultTermImpl
import com.github.vatbub.openthesaurus.apiclient.SimilarResultTermImpl
import com.github.vatbub.openthesaurus.util.Either
import com.github.vatbub.openthesaurus.util.left
import com.github.vatbub.openthesaurus.util.right
import java.io.Closeable
import java.util.Locale
import xyz.nextn.levenshteindistance.LevenshteinDistance

class BigHugeThesaurusProvider(
    endpoint: String = "https://words.bighugelabs.com/api/2/"
) : DataProvider, Closeable {
    override val screenName: String = "BigHugeThesaurus"
    override val internalName: String = "BigHugeThesaurus"
    override val supportedLocales: List<Locale> = listOf(Locale.ENGLISH)

    private val client by lazy { BigHugeThesaurusClient(endpoint) }

    override suspend fun requestTerm(term: String, searchLocale: Locale): Either<Response, ApiError> {
        if (searchLocale !in supportedLocales) throw IllegalArgumentException("Locale $searchLocale not supported")
        return client.request(
            BigHugeThesaurusRequest(term)
        ).leftOr {
            return if (it.responseCode == 404) ResponseImpl(listOf()).left()
            else it.right()
        }.toResponse(term).left()
    }

    private fun BigHugeThesaurusResult.toResponse(searchTerm: String): Response {
        val synonyms = wordClasses.flatMap { it.value.synonyms }.distinct().map { ResultTermImpl(it) }
        val antonyms = wordClasses.flatMap { it.value.antonyms }.distinct().map { ResultTermImpl(it) }
        val similarTerms = wordClasses.flatMap { it.value.similar + it.value.related + it.value.userSuggestions }
            .distinct()
            .map { term ->
                SimilarResultTermImpl(
                    term,
                    LevenshteinDistance.calculate(searchTerm, term)
                )
            }
        return object : Response by ResponseImpl(synonyms),
            ResponseWithSimilarTerms by ResponseWithSimilarTermsImpl(similarTerms),
            ResponseWithAntonyms by ResponseWithAntonymsImpl(antonyms) {}
    }

    override fun close() {
        client.close()
    }
}
