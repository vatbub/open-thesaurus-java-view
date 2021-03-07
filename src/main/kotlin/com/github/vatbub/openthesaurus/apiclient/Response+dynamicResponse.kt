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

fun Response.Companion.dynamicResponse(
    synonyms: List<ResultTerm>,
    similarTerms: List<SimilarResultTerm>? = null,
    substringTerms: List<ResultTerm>? = null,
    baseForms: List<ResultTerm>? = null
): Response =
    if (similarTerms != null && substringTerms != null && baseForms != null)
        object : Response by ResponseImpl(synonyms),
            ResponseWithSimilarTerms by ResponseWithSimilarTermsImpl(similarTerms),
            ResponseWithSubstringTerms by ResponseWithSubstringTermsImpl(substringTerms),
            ResponseWithBaseForms by ResponseWithBaseFormsImpl(baseForms) {}
    else if (similarTerms != null && substringTerms != null)
        object : Response by ResponseImpl(synonyms),
            ResponseWithSimilarTerms by ResponseWithSimilarTermsImpl(similarTerms),
            ResponseWithSubstringTerms by ResponseWithSubstringTermsImpl(substringTerms) {}
    else if (similarTerms != null && baseForms != null)
        object : Response by ResponseImpl(synonyms),
            ResponseWithSimilarTerms by ResponseWithSimilarTermsImpl(similarTerms),
            ResponseWithBaseForms by ResponseWithBaseFormsImpl(baseForms) {}
    else if (substringTerms != null && baseForms != null)
        object : Response by ResponseImpl(synonyms),
            ResponseWithSubstringTerms by ResponseWithSubstringTermsImpl(substringTerms),
            ResponseWithBaseForms by ResponseWithBaseFormsImpl(baseForms) {}
    else if (similarTerms != null)
        object : Response by ResponseImpl(synonyms),
            ResponseWithSimilarTerms by ResponseWithSimilarTermsImpl(similarTerms) {}
    else if (substringTerms != null)
        object : Response by ResponseImpl(synonyms),
            ResponseWithSubstringTerms by ResponseWithSubstringTermsImpl(substringTerms) {}
    else if (baseForms != null)
        object : Response by ResponseImpl(synonyms),
            ResponseWithBaseForms by ResponseWithBaseFormsImpl(baseForms) {}
    else ResponseImpl(synonyms)
