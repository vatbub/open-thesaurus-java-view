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
package com.github.vatbub.openthesaurus.apiclient.duden

import com.github.vatbub.openthesaurus.apiclient.ApiError
import com.github.vatbub.openthesaurus.apiclient.DataProvider
import com.github.vatbub.openthesaurus.apiclient.Response
import com.github.vatbub.openthesaurus.apiclient.ResponseImpl
import com.github.vatbub.openthesaurus.apiclient.ResultTermImpl
import com.github.vatbub.openthesaurus.util.Either
import com.github.vatbub.openthesaurus.util.left
import com.github.vatbub.openthesaurus.util.right
import java.io.Closeable
import java.util.Locale

class DudenProvider(
    private val endpoint: String = "https://api.duden.de/v1/synonyms/",
) : DataProvider, Closeable {
    override val screenName: String = "Duden Synonyme"
    override val internalName: String = "Duden"
    override val supportedLocales: List<Locale> = listOf(Locale.GERMAN)

    private val client by lazy { DudenClient(endpoint) }

    override suspend fun requestTerm(term: String, searchLocale: Locale): Either<Response, ApiError> {
        if (searchLocale !in supportedLocales) throw IllegalArgumentException("Locale $searchLocale not supported")
        return client.request(
            DudenRequest(term)
        ).leftOr {
            return if (it.responseCode == 404) ResponseImpl(listOf()).left()
            else it.right()
        }.toResponse().left()
    }

    private fun DudenResult.toResponse() = ResponseImpl(
        meanings.flatMap { it.synonyms }.distinct().map { ResultTermImpl(it) }
    )

    override fun close() {
        client.close()
    }
}
