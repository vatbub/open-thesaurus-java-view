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

class ResponseImpl(override val synonymSets: List<ResultTerm>) : Response

class ResponseWithAntonymsImpl(
    override val antonyms: List<ResultTerm>
) : ResponseWithAntonyms

class ResponseWithSimilarTermsImpl(
    override val similarTerms: List<SimilarResultTerm>
) : ResponseWithSimilarTerms

class ResponseWithSubstringTermsImpl(
    override val substringTerms: List<ResultTerm>
) : ResponseWithSubstringTerms

class ResponseWithBaseFormsImpl(
    override val baseForms: List<ResultTerm>
) : ResponseWithBaseForms
