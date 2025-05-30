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

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.vatbub.openthesaurus.util.Either
import com.github.vatbub.openthesaurus.util.left
import java.util.Locale

fun DataProvider.cacheResults(cacheSize: Long = 50L): DataProvider = object : DataProvider {
    override val screenName: String
        get() = this@cacheResults.screenName
    override val supportedLocales: List<Locale>
        get() = this@cacheResults.supportedLocales

    private val cache = Caffeine.newBuilder()
        .maximumSize(cacheSize)
        .build<CacheKey, Response>()

    override suspend fun requestTerm(term: String, searchLocale: Locale): Either<Response, ApiError> {
        val key = CacheKey(term, searchLocale)
        cache.getIfPresent(key)?.let { return it.left() }
        val result = this@cacheResults.requestTerm(term, searchLocale)
        if (result is Either.Left<Response>)
            cache.put(key, result.value)
        return result
    }
}

private data class CacheKey(val term: String, val searchLocale: Locale)
