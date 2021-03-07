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

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("USELESS_IS_CHECK")
class DynamicResponseTest {
    @Test
    fun noOptionalParameters(){
        val response = Response.dynamicResponse(listOf())
        assertTrue(response is Response)
        assertFalse(response is ResponseWithSimilarTerms)
        assertFalse(response is ResponseWithSubstringTerms)
        assertFalse(response is ResponseWithBaseForms)
    }

    @Test
    fun allParameters(){
        val response = Response.dynamicResponse(
            synonyms = listOf(),
            similarTerms = listOf(),
            substringTerms = listOf(),
            baseForms = listOf()
        )
        assertTrue(response is Response)
        assertTrue(response is ResponseWithSimilarTerms)
        assertTrue(response is ResponseWithSubstringTerms)
        assertTrue(response is ResponseWithBaseForms)
    }

    @Test
    fun similarTermsAndSubstringTerms(){
        val response = Response.dynamicResponse(
            synonyms = listOf(),
            similarTerms = listOf(),
            substringTerms = listOf()
        )
        assertTrue(response is Response)
        assertTrue(response is ResponseWithSimilarTerms)
        assertTrue(response is ResponseWithSubstringTerms)
        assertFalse(response is ResponseWithBaseForms)
    }

    @Test
    fun similarTermsAndBaseForms(){
        val response = Response.dynamicResponse(
            synonyms = listOf(),
            similarTerms = listOf(),
            baseForms = listOf()
        )
        assertTrue(response is Response)
        assertTrue(response is ResponseWithSimilarTerms)
        assertFalse(response is ResponseWithSubstringTerms)
        assertTrue(response is ResponseWithBaseForms)
    }

    @Test
    fun substringTermsAndBaseForms(){
        val response = Response.dynamicResponse(
            synonyms = listOf(),
            substringTerms = listOf(),
            baseForms = listOf()
        )
        assertTrue(response is Response)
        assertFalse(response is ResponseWithSimilarTerms)
        assertTrue(response is ResponseWithSubstringTerms)
        assertTrue(response is ResponseWithBaseForms)
    }

    @Test
    fun similarTerms(){
        val response = Response.dynamicResponse(
            synonyms = listOf(),
            similarTerms = listOf()
        )
        assertTrue(response is Response)
        assertTrue(response is ResponseWithSimilarTerms)
        assertFalse(response is ResponseWithSubstringTerms)
        assertFalse(response is ResponseWithBaseForms)
    }

    @Test
    fun substringTerms(){
        val response = Response.dynamicResponse(
            synonyms = listOf(),
            substringTerms = listOf(),
        )
        assertTrue(response is Response)
        assertFalse(response is ResponseWithSimilarTerms)
        assertTrue(response is ResponseWithSubstringTerms)
        assertFalse(response is ResponseWithBaseForms)
    }

    @Test
    fun baseForms(){
        val response = Response.dynamicResponse(
            synonyms = listOf(),
            baseForms = listOf()
        )
        assertTrue(response is Response)
        assertFalse(response is ResponseWithSimilarTerms)
        assertFalse(response is ResponseWithSubstringTerms)
        assertTrue(response is ResponseWithBaseForms)
    }
}
