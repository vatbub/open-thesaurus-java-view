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

import com.beust.klaxon.Klaxon
import com.github.vatbub.openthesaurus.apiclient.OpenThesaurusApiError.Cause.*
import com.github.vatbub.openthesaurus.logging.logger
import com.github.vatbub.openthesaurus.util.Either
import com.github.vatbub.openthesaurus.util.left
import com.github.vatbub.openthesaurus.util.right
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.io.Closeable

class OpenThesaurusClient(
    private val endpoint: String = "https://www.openthesaurus.de/",
    private val cacheSize: Int = 50
) : Closeable {
    companion object {
        private val client by lazy { OkHttpClient() }
        private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    }

    private data class CacheEntry(val timestamp: Long, val result: OpenThesaurusResult)

    private var cache: MutableMap<OpenThesaurusRequest, CacheEntry> = mutableMapOf()

    suspend fun request(request: OpenThesaurusRequest): Either<OpenThesaurusResult, OpenThesaurusApiError> {
        try {
            cache[request]?.let { return it.result.left() }

            val finalUrl = with(request) {
                endpoint.toHttpUrlOrNull()!!
                    .newBuilder()
                    .addPathSegment("synonyme")
                    .addPathSegment("search")
                    .addQueryParameter("q", term)
                    .addQueryParameter("format", "application/json")
                    .addQueryParameter("similar", similarTerms)
                    .addSubstringConfig(substring)
                    .addStartsWithConfig(startsWith)
                    .addQueryParameter("supersynsets", superSynonymSets)
                    .addQueryParameter("subsynsets", subSynonymSets)
                    .addQueryParameter("baseform", baseForm)
                    .build()
            }

            logger.info("Sending request: $finalUrl")

            val httpRequest = Request.Builder()
                .url(finalUrl)
                .get()
                .build()

            val result = client.newCall(httpRequest).await().use { response ->
                if (response.code >= 300) {
                    val stringContent = response.body?.charStream()?.readText()
                    return OpenThesaurusApiError(
                        responseCode = response.code,
                        responseContent = stringContent,
                        cause = when (response.code) {
                            in 300..399 -> Redirect
                            in 400..499 -> ClientError
                            in 500..599 -> ServerError
                            else -> Other
                        }
                    ).right()

                }

                val reader = response.body!!.charStream()
                val jsonObject = Klaxon().parseJsonObject(reader)
                OpenThesaurusResult.fromJson(jsonObject)
            }
            addToCache(request, result)
            return result.left()
        } catch (throwable: Throwable) {
            return OpenThesaurusApiError(Exception, throwable = throwable).right()
        }
    }

    private fun addToCache(request: OpenThesaurusRequest, result: OpenThesaurusResult) {
        cache[request] = CacheEntry(System.currentTimeMillis(), result)
        if (cache.size > cacheSize) {
            val sortedList = cache.toList().sortedBy { it.second.timestamp }.toMutableList()
            while (sortedList.size > cacheSize)
                sortedList.removeFirst()
            cache = sortedList.toMap().toMutableMap()
        }
    }

    override fun close() {
        client.dispatcher.executorService.shutdown()
    }
}

internal fun HttpUrl.Builder.addQueryParameter(name: String, value: Boolean = false) =
    addQueryParameter(name, value.toString())

internal fun HttpUrl.Builder.addQueryParameter(name: String, value: Int) =
    addQueryParameter(name, value.toString())
