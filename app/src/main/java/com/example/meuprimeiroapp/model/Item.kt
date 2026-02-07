package com.example.meuprimeiroapp.model

/**
 * Represents a simple item with an ID.
 *
 * @property id The unique identifier for the item.
 */
data class Item(
    val id: String,
    val value: ItemValue
)

/**
 * Represents the detailed values of an item.
 *
 * @property id The unique identifier of the item.
 * @property name The name of the person.
 * @property surname The surname of the person.
 * @property profession The profession of the person.
 * @property imageUrl The URL of the person's image.
 * @property age The age of the person.
 * @property location The location of the person.
 */
data class ItemValue(
    val id: String,
    val name: String,
    val surname: String,
    val profession: String,
    val imageUrl: String,
    val age: Int,
    val location: ItemLocation?
) {
    val fullName: String
        get() = "$name $surname"
}

/**
 * Represents a geographical location.
 *
 * @property name The name of the location.
 * @property latitude The latitude of the location.
 * @property longitude The longitude of the location.
 */
data class ItemLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double
)
