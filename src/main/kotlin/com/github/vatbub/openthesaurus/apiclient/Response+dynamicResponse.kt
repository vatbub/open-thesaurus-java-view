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
