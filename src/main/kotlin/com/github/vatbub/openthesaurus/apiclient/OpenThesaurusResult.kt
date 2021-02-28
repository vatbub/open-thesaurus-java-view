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

import com.beust.klaxon.JsonObject

data class OpenThesaurusResult(
    val metaData: OpenThesaurusMetaData,
    val synonymSets: List<OpenThesaurusSynonymSet>,
    val similarTerms: List<OpenThesaurusTerm>?,
    val substringTerms: List<OpenThesaurusTerm>?,
    val baseForms: List<OpenThesaurusTerm>?
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): OpenThesaurusResult {

            val metaData = OpenThesaurusMetaData.fromJson(jsonObject.obj("metaData")!!)
            val synonymSets = jsonObject.array<JsonObject>("synsets")!!
                .map { OpenThesaurusSynonymSet.fromJson(it) }
            val similarTerms = jsonObject.array<JsonObject>("similarterms")
                ?.map { OpenThesaurusTerm.fromJson(it) }
            val substringTerms = jsonObject.array<JsonObject>("substringterms")
                ?.map { OpenThesaurusTerm.fromJson(it) }
            val baseForms = jsonObject.array<String>("baseforms")
                ?.map { OpenThesaurusTerm(it, null, null) }


            return OpenThesaurusResult(
                metaData,
                synonymSets,
                similarTerms,
                substringTerms,
                baseForms
            )
        }
    }
}

data class OpenThesaurusMetaData(
    val apiVersion: String,
    val warning: String?,
    val copyright: String?,
    val license: String?,
    val source: String,
    val date: String
) {
    companion object {
        fun fromJson(jsonObject: JsonObject) = OpenThesaurusMetaData(
            apiVersion = jsonObject.string("apiVersion")!!,
            warning = jsonObject.string("warning"),
            copyright = jsonObject.string("copyright"),
            license = jsonObject.string("license"),
            source = jsonObject.string("source")!!,
            date = jsonObject.string("date")!!
        )
    }
}

data class OpenThesaurusSynonymSet(val id: Int, val categories: List<String>, val terms: List<OpenThesaurusTerm>) {
    companion object {
        fun fromJson(jsonObject: JsonObject) = OpenThesaurusSynonymSet(
            id = jsonObject.int("id")!!,
            categories = jsonObject.array("categories")!!,
            terms = jsonObject.array<JsonObject>("terms")!!.map { OpenThesaurusTerm.fromJson(it) }
        )
    }
}

data class OpenThesaurusTerm(val term: String, val level: String?, val distance: Int?) {
    companion object {
        fun fromJson(jsonObject: JsonObject) = OpenThesaurusTerm(
            term = jsonObject.string("term")!!,
            level = jsonObject.string("level"),
            distance = jsonObject.int("distance")
        )
    }

    override fun toString() = if (level == null) term else "$term ($level)"
}
