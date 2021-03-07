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

import okhttp3.HttpUrl

data class OpenThesaurusRequest(
    val term: String,
    val similarTerms: Boolean = true,
    val substring: SubstringConfig = SubstringConfig.Disabled,
    val startsWith: StartsWithConfig = StartsWithConfig.Disabled,
    val superSynonymSets: Boolean = true,
    val subSynonymSets: Boolean = true,
    val baseForm: Boolean = true
)

sealed class SubstringConfig {
    abstract fun addToHttpUrlBuilder(builder: HttpUrl.Builder): HttpUrl.Builder

    object Disabled : SubstringConfig() {
        override fun addToHttpUrlBuilder(builder: HttpUrl.Builder): HttpUrl.Builder =
            builder.addQueryParameter("substring", false)
    }

    data class Enabled(val fromResults: Int = 0, val maxResults: Int = 10) : SubstringConfig() {
        init {
            require(maxResults <= 250) { "maxResults must not exceed 250." }
        }

        override fun addToHttpUrlBuilder(builder: HttpUrl.Builder): HttpUrl.Builder =
            builder.addQueryParameter("substring", true)
                .addQueryParameter("substringFromResults", fromResults)
                .addQueryParameter("substringMaxResults", maxResults)
    }
}

sealed class StartsWithConfig {
    abstract fun addToHttpUrlBuilder(builder: HttpUrl.Builder): HttpUrl.Builder

    object Disabled : StartsWithConfig() {
        override fun addToHttpUrlBuilder(builder: HttpUrl.Builder): HttpUrl.Builder =
            builder.addQueryParameter("startswith", false)
    }

    data class Enabled(val fromResults: Int = 0, val maxResults: Int = 10) : StartsWithConfig() {
        init {
            require(maxResults <= 250) { "maxResults must not exceed 250." }
        }

        override fun addToHttpUrlBuilder(builder: HttpUrl.Builder): HttpUrl.Builder =
            builder.addQueryParameter("startswith", true)
                .addQueryParameter("startsWithFromResults", fromResults)
                .addQueryParameter("startsWithMaxResults", maxResults)
    }
}

fun HttpUrl.Builder.addSubstringConfig(config: SubstringConfig): HttpUrl.Builder = config.addToHttpUrlBuilder(this)

fun HttpUrl.Builder.addStartsWithConfig(config: StartsWithConfig): HttpUrl.Builder = config.addToHttpUrlBuilder(this)
