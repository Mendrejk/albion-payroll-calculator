import okio.FileSystem
import okio.Path.Companion.toPath

data class Participant(val name: String, var returnPoints: Int = 0, var itemsTotal: Int = 0, var cashTotal: Int = 0, var returnTotal: Int = 0)
data class Content(val id: Int, val itemsTotal: Int, val cashTotal: Int, val organizer: Participant?, val participants: List<Participant>)
data class Input(val contents: List<Content>, val recruitments: List<Pair<String, Int>>)

var participants = mutableMapOf<String, Participant>()

fun load_input_file(): Input {
    // 1. Load the "wejscie.txt" file
    // 2. Remove all blank lines and lines starting with "#"
    // 3. Parse the file into a list of Content and a list of Pair<String, Int>
    // 4. Return the Input object

    val lines = mutableListOf<String>()
    FileSystem.SYSTEM.read("wejscie.txt".toPath()) {
        while (true)
        {
            val line = readUtf8Line() ?: break
            if (line.isNotBlank() && !line.startsWith("#"))
            {
                lines.add(line)
            }
        }
    }

    val (contentLines, recruitmentLines) = lines.fold(mutableListOf<MutableList<String>>()) { acc, line ->
        if (line.startsWith("KONTENTY:") || line.startsWith("REKRUTACJA:")) {
            acc.add(mutableListOf())
        }
        else {
            acc.lastOrNull()?.add(line)
        }
        acc
    }

    val groupedContentLines = contentLines.fold(mutableListOf<MutableList<String>>()) { acc, line ->
        if (line.matches(Regex("\\d+:.*"))) {
            acc.add(mutableListOf(line))
        } else {
            acc.lastOrNull()?.add(line)
        }
        acc
    }

    val contents = groupedContentLines.map { group ->
        var currentContentId = 0
        var currentOrganizer: Participant? = null
        val currentParticipants = mutableListOf<Participant>()
        var currentItemsTotal = 0
        var currentCashTotal = 0

        group.forEach { line ->
            when {
                line.matches(Regex("\\d+:.*")) -> {
                    line.split(":").map { it.trim() }.takeIf { it.size == 2 }?.let { parts ->
                        currentContentId = parts[0].toInt()
                        currentOrganizer = parts[1].takeIf { it.isNotEmpty() }?.let {
                            participants.getOrPut(it) { Participant(it) }
                        }
                    }
                }
                else -> {
                    line.split(":").map { it.trim() }.takeIf { it.size == 2 }?.let { parts ->
                        parts[1].split(",").map { it.trim() }.takeIf { it.size >= 3 }?.let { collectionParts ->
                            currentItemsTotal += collectionParts[0].toInt()
                            currentCashTotal += collectionParts[1].toIntOrNull() ?: 0
                            collectionParts.drop(2).forEach { participantName -> // TODO - Check if participantName does not include (50%)
                                participants.getOrPut(participantName) { Participant(participantName) }.also {
                                    currentParticipants.add(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    TODO()
}

fun calculate_payroll() {
    // 1. Load the input file
}

fun main() {
}
