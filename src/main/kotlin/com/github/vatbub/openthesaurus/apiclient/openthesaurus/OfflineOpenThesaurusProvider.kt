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

import com.github.vatbub.openthesaurus.apiclient.ApiError
import com.github.vatbub.openthesaurus.apiclient.DataProvider
import com.github.vatbub.openthesaurus.apiclient.Response
import com.github.vatbub.openthesaurus.apiclient.ResultTerm
import com.github.vatbub.openthesaurus.apiclient.ResultTermImpl
import com.github.vatbub.openthesaurus.apiclient.SimilarResultTerm
import com.github.vatbub.openthesaurus.apiclient.SimilarResultTermImpl
import com.github.vatbub.openthesaurus.apiclient.dynamicResponse
import com.github.vatbub.openthesaurus.util.Either
import com.github.vatbub.openthesaurus.util.left
import com.github.vatbub.openthesaurus.util.right
import java.util.Locale
import java.util.zip.ZipInputStream
import xyz.nextn.levenshteindistance.LevenshteinDistance

class OfflineOpenThesaurusProvider(
    val levenshteinTheshold: Int = 3
) : DataProvider {
    override val screenName: String = "OpenThesaurus.de (Offline)"
    override val internalName: String = "Offline OpenThesaurus.de"
    override val supportedLocales: List<Locale> = listOf(Locale.GERMAN)

    fun <T> iterateOverOfflineDatabase(block: (Sequence<List<ResultTerm>>) -> T): Either<T, ApiError> {
        OpenThesaurusDatabaseCache.zipDownloadDestination.inputStream().use { fileInputStream ->
            ZipInputStream(fileInputStream).use { zipStream ->
                var entry = zipStream.nextEntry
                while (entry != null && entry.name != "openthesaurus.txt") {
                    entry = zipStream.nextEntry
                }
                if (entry == null) return ApiError(
                    ApiError.Cause.Other,
                    responseContent = "Illegal Zip file format"
                ).right()
                zipStream.bufferedReader().use { reader ->
                    val sequence = reader.lineSequence()
                        .filterNot { it.startsWith("#") }
                        .map { line ->
                            line.split(";(?=\\S)".toRegex())
                                .map { unparsedTerm ->
                                    if (unparsedTerm.endsWith(")")) {
                                        val beginningOfNote = unparsedTerm.lastIndexOf("(")
                                        if (beginningOfNote == -1) {
                                            ResultTermImpl(unparsedTerm.trim())
                                        } else {
                                            val actualTerm = unparsedTerm.substring(0, beginningOfNote).trim()
                                            val note = unparsedTerm.substring(beginningOfNote + 1).dropLast(1)
                                            ResultTermImpl(actualTerm, note)
                                        }
                                    } else ResultTermImpl(unparsedTerm)
                                }
                        }

                    return block(sequence).left()
                }
            }
        }
    }

    override suspend fun requestTerm(term: String, searchLocale: Locale): Either<Response, ApiError> {
        if (searchLocale !in supportedLocales) throw IllegalArgumentException("Locale $searchLocale not supported")
        OpenThesaurusDatabaseCache.downloadIfNecessary()

        return iterateOverOfflineDatabase { sequence ->
            val exactMatches = mutableListOf<ResultTerm>()
            val substringMatches = mutableListOf<ResultTerm>()
            val levenshteinMatches = mutableListOf<SimilarResultTerm>()

            sequence.forEach { termLine ->
                if (termLine.any { it.term == term }) {
                    exactMatches.addAll(termLine)
                    return@forEach
                }
                if (termLine.any { it.term.contains(term) }) {
                    substringMatches.addAll(termLine)
                    return@forEach
                }

                val termsWithLevenshteinDistances = termLine.map { termFromLine ->
                    SimilarResultTermImpl(
                        termFromLine.term,
                        LevenshteinDistance.calculate(term, termFromLine.term),
                        termFromLine.additionalNote
                    )
                }
                if (termsWithLevenshteinDistances.any { it.distance < levenshteinTheshold }) {
                    levenshteinMatches.addAll(termsWithLevenshteinDistances)
                }
            }

            val substringMatchesWithDistances = substringMatches.map {
                SimilarResultTermImpl(
                    it.term,
                    LevenshteinDistance.calculate(term, it.term),
                    it.additionalNote
                )
            }

            Response.dynamicResponse(
                exactMatches.filterNot { it.term == term }.distinct(),
                (levenshteinMatches + substringMatchesWithDistances).distinct(),
            )
        }
    }
}
