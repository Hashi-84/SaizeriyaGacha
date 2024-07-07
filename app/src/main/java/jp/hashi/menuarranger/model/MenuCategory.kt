package jp.hashi.menuarranger.model

import kotlinx.serialization.Serializable

@Serializable
data class MenuCategory(
    val category: String,
    val name: String
) {
    override fun equals(other: Any?): Boolean {
        return this === other ||
                other is MenuCategory
                && other.category == category
                && other.name == name
    }
}