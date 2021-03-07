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
import java.util.*

interface DataProvider {
    val screenName: String

    val supportedLocales: List<Locale>

    suspend fun requestTerm(term: String, searchLocale: Locale): Either<Response, ApiError>

    companion object {
        val knownImplementations: List<DataProvider> by lazy {
            listOf(
                OpenThesaurusProvider()
            )
        }
    }
}
