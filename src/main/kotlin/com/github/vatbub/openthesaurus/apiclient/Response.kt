package com.github.vatbub.openthesaurus.apiclient

interface Response {
    val synonymSets: List<ResultTerm>

    companion object
}

interface ResponseWithSimilarTerms {
    val similarTerms: List<SimilarResultTerm>
}

interface ResponseWithSubstringTerms {
    val substringTerms: List<ResultTerm>
}

interface ResponseWithBaseForms {
    val baseForms: List<ResultTerm>
}
