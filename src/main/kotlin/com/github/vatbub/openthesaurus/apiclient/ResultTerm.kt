package com.github.vatbub.openthesaurus.apiclient

interface ResultTerm {
    val term: String
    val additionalNote: String?
}

interface SimilarResultTerm : ResultTerm {
    val distance: Int
}
