package com.example.fasopronostics.data.model

enum class SportType(val displayName: String, val iconName: String) {
    ALL("Tous les sports", "sports"),
    FOOTBALL("Football", "sports_soccer"),
    BASKETBALL("Basketball", "sports_basketball"),
    TENNIS("Tennis", "sports_tennis"),
    HANDBALL("Handball", "sports_handball"),
    VOLLEYBALL("Volleyball", "sports_volleyball"),
    HOCKEY("Hockey", "sports_hockey");

    companion object {
        fun fromString(value: String): SportType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: FOOTBALL
        }
    }
}
