// Formatting utilities

fun formatNumberWithSpaces(number: Int): String {
    return number.toString().reversed().chunked(3).joinToString(" ").reversed()
}

fun formatNumberWithSpacesAndK(number: Int): String {
    return "${formatNumberWithSpaces(number / 1000)}k"
}

fun parseParticipant(participantString: String): Pair<String, Boolean> {
    val trimmedName = participantString.trim()
    val hasReducedShare = trimmedName.endsWith("(50%)")
    val name = if (hasReducedShare) {
        trimmedName.removeSuffix("(50%)").trim()
    } else {
        trimmedName
    }
    return Pair(name, !hasReducedShare)
}
