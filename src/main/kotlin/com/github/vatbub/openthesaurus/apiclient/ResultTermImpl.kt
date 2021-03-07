package com.github.vatbub.openthesaurus.apiclient

internal open class ResultTermImpl(override val term: String, override val additionalNote: String? = null) : ResultTerm

internal class SimilarResultTermImpl(
    term: String,
    override val distance: Int,
    additionalNote: String? = null
) : ResultTermImpl(term, additionalNote), SimilarResultTerm
