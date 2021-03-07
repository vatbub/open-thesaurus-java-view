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

import com.github.vatbub.openthesaurus.util.Either
import com.github.vatbub.openthesaurus.util.left
import java.util.*

fun DataProvider.cacheResults(cacheSize: Int = 50): DataProvider = object : DataProvider {
    override val screenName: String
        get() = this@cacheResults.screenName
    override val supportedLocales: List<Locale>
        get() = this@cacheResults.supportedLocales

    private var cache: MutableMap<CacheKey, CacheEntry> = mutableMapOf()

    override suspend fun requestTerm(term: String, searchLocale: Locale): Either<Response, ApiError> {
        cache[CacheKey(term, searchLocale)]?.let { return it.result.left() }
        val result = this@cacheResults.requestTerm(term, searchLocale)
        if (result is Either.Left<Response>)
            addToCache(CacheKey(term, searchLocale), result.value)
        return result
    }

    private fun addToCache(request: CacheKey, result: Response) {
        cache[request] = CacheEntry(System.currentTimeMillis(), result)
        if (cache.size > cacheSize) {
            val sortedList = cache.toList().sortedBy { it.second.timestamp }.toMutableList()
            while (sortedList.size > cacheSize)
                sortedList.removeFirst()
            cache = sortedList.toMap().toMutableMap()
        }
    }
}

private data class CacheKey(val term: String, val searchLocale: Locale)
private data class CacheEntry(val timestamp: Long, val result: Response)
