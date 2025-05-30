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

import com.beust.klaxon.JsonObject

data class BigHugeThesaurusResult(
    val wordClasses: Map<String, BigHugeThesaurusClassResult>
) {
    companion object {
        fun fromJson(jsonObject: JsonObject): BigHugeThesaurusResult {
            return BigHugeThesaurusResult(
                jsonObject
                    .filter { it.value is JsonObject }
                    .mapValues { BigHugeThesaurusClassResult.fromJson(it.value as JsonObject) }
            )
        }
    }
}

data class BigHugeThesaurusClassResult(
    val synonyms: List<String>,
    val antonyms: List<String>,
    val related: List<String>,
    val similar: List<String>,
    val userSuggestions: List<String>
) {
    companion object {
        fun fromJson(jsonObject: JsonObject) = BigHugeThesaurusClassResult(
            synonyms = jsonObject.array<String>("syn")?.toList().orEmpty(),
            antonyms = jsonObject.array<String>("ant")?.toList().orEmpty(),
            related = jsonObject.array<String>("rel")?.toList().orEmpty(),
            similar = jsonObject.array<String>("sim")?.toList().orEmpty(),
            userSuggestions = jsonObject.array<String>("usr")?.toList().orEmpty(),
        )
    }
}
