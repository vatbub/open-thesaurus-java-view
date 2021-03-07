package com.github.vatbub.openthesaurus.apiclient

class ResponseImpl(override val synonymSets: List<ResultTerm>) : Response

class ResponseWithSimilarTermsImpl(
    override val similarTerms: List<SimilarResultTerm>
) : ResponseWithSimilarTerms

class ResponseWithSubstringTermsImpl(
    override val substringTerms: List<ResultTerm>
) : ResponseWithSubstringTerms

class ResponseWithBaseFormsImpl(
    override val baseForms: List<ResultTerm>
) : ResponseWithBaseForms
