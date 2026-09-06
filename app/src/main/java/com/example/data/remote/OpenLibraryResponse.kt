package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenLibrarySearchResponse(
    @Json(name = "numFound") val numFound: Int? = 0,
    @Json(name = "docs") val docs: List<OpenLibraryDoc>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class OpenLibraryDoc(
    @Json(name = "key") val key: String? = "",
    @Json(name = "title") val title: String? = "Untitled",
    @Json(name = "author_name") val authorName: List<String>? = emptyList(),
    @Json(name = "number_of_pages_median") val numberOfPagesMedian: Int? = null,
    @Json(name = "cover_i") val coverI: Long? = null,
    @Json(name = "first_publish_year") val firstPublishYear: Int? = null
) {
    val displayAuthor: String
        get() = authorName?.firstOrNull() ?: "Unknown Author"

    val displayPages: Int
        get() = numberOfPagesMedian?.takeIf { it > 0 } ?: 250

    val coverUrl: String
        get() = if (coverI != null && coverI > 0) {
            "https://covers.openlibrary.org/b/id/$coverI-M.jpg"
        } else {
            ""
        }
}
