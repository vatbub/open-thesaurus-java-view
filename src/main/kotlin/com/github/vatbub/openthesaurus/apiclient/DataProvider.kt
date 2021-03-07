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
