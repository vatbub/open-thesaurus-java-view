package com.github.vatbub.openthesaurus.apiclient

import com.github.vatbub.openthesaurus.apiclient.openthesaurus.*
import com.github.vatbub.openthesaurus.util.Either
import com.github.vatbub.openthesaurus.util.left
import com.github.vatbub.openthesaurus.util.right
import java.io.Closeable
import java.util.*

class OpenThesaurusProvider(
    endpoint: String = "https://www.openthesaurus.de/",
    cacheSize: Int = 50
) : DataProvider, Closeable {
    override val screenName: String = "OpenThesaurus.de"
    override val supportedLocales: List<Locale> = listOf(Locale.GERMAN)

    private val client by lazy { OpenThesaurusClient(endpoint, cacheSize) }

    override suspend fun requestTerm(term: String, searchLocale:Locale): Either<Response, ApiError> {
        if (searchLocale !in supportedLocales) throw IllegalArgumentException("Locale $searchLocale not supported")
        return client.request(
            OpenThesaurusRequest(
                term,
                superSynonymSets = false,
                subSynonymSets = false
            )
        ).leftOr { return it.right() }
            .toResponse().left()
    }

    private fun OpenThesaurusResult.toResponse(): Response = Response.dynamicResponse(
        synonyms = this.synonymSets.toResultTermList(),
        similarTerms = this.similarTerms?.toSimilarTermList(),
        substringTerms = this.substringTerms?.toResultTermList(),
        baseForms = this.baseForms?.toResultTermList()
    )

    private fun List<OpenThesaurusSynonymSet>.toResultTermList(): List<ResultTerm> =
        this.map { it.terms }
            .flatten()
            .map { ResultTermImpl(it.term, it.level) }

    @JvmName("toResultTermListOpenThesaurusTerm")
    private fun List<OpenThesaurusTerm>.toResultTermList(): List<ResultTerm> =
        this.map { ResultTermImpl(it.term, it.level) }

    private fun List<OpenThesaurusTerm>.toSimilarTermList(): List<SimilarResultTerm> =
        this.map { SimilarResultTermImpl(it.term, it.distance!!, it.level) }

    override fun close() {
        client.close()
    }
}
