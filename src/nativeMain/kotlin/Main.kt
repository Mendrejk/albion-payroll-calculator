import okio.FileSystem
import okio.Path.Companion.toPath


data class Participant(
    val name: String,
    var returnPoints: Int = 0,
    var itemsAfterTax: Int = 0,
    var cashAfterTax: Int = 0,
    var returnsTotal: Int = 0
)

data class HaulInput(val itemsBeforeTax: Int, val cashBeforeTax: Int, val participants: List<Participant>)

data class Haul(
    val itemsAfterTax: Int,
    val cashAfterTax: Int,
    val itemsTax: Int,
    val cashTax: Int,
    val returnsFromItems: Int,
    val returnsFromCash: Int,
    val participants: List<Participant>
)

data class ContentInput(
    val id: Int, val organizer: Participant?, val returnPointsPerHaul: Double, val hauls: List<Haul>
)

data class Content(
    val id: Int,
    val itemsTaxTotal: Int,
    val cashTaxTotal: Int,
    val returnsFromItemsTotal: Int,
    val returnsFromCashTotal: Int,
    val organizer: Participant?,
    val participants: List<Participant>
)

data class Input(val contents: List<Content>, val recruitments: List<Pair<String, Int>>)

var participants = mutableMapOf<String, Participant>()

fun load_input_file(): Input {
    // 1. Load the "wejscie.txt" file
    // 2. Remove all blank lines and lines starting with "#"
    // 3. Parse the file into a list of Content and a list of Pair<String, Int>
    // 4. Return the Input object

    val lines = mutableListOf<String>()
    FileSystem.SYSTEM.read("wejscie.txt".toPath()) {
        while (true) {
            val line = readUtf8Line() ?: break
            if (line.isNotBlank() && !line.startsWith("#")) {
                lines.add(line)
            }
        }
    }

    val (contentsLines, recruitmentsLines) = lines.fold(mutableListOf<MutableList<String>>()) { acc, line ->
        if (line.startsWith("KONTENTY:") || line.startsWith("REKRUTACJA:")) {
            acc.add(mutableListOf())
        } else {
            acc.lastOrNull()?.add(line)
        }
        acc
    }

    val groupedContentsLines = contentsLines.fold(mutableListOf<MutableList<String>>()) { acc, line ->
        if (line.matches(Regex("\\d+:.*"))) {
            acc.add(mutableListOf(line))
        } else {
            acc.lastOrNull()?.add(line)
        }
        acc
    }

    // TODO: add checks for invalid shape of the wejscie.txt file
    // if something is wrong, it should be logged
    val contents = groupedContentsLines.map { contentLines ->
        val contentInput = run {
            val (contentId, organizer) = run {
                val contentHeader = contentLines.first().split(":").map { it.trim() }
                val contentId = contentHeader.getOrNull(0)?.toIntOrNull() ?: 0
                val organizer = contentHeader.getOrNull(1)?.takeIf { it.isNotEmpty() }?.let {
                    val organizer = participants.getOrPut(it) { Participant(it) }
                    organizer.returnPoints += 1
                    organizer
                }
                Pair(contentId, organizer)
            }

            val haulsLines = contentLines.drop(1)
            val returnPointsPerHaul = organizer?.let {
                val numberOfHauls = haulsLines.count { it.split(":").size == 2 }
                if (numberOfHauls > 0) (1.0 / numberOfHauls.toDouble()) else 0.0
            } ?: 0.0

            val haulInputs = haulsLines.mapNotNull { haulLine ->
                haulLine.split(":").map { it.trim() }.takeIf { it.size == 2 }?.let { (_, haulData) ->
                    haulData.split(",").map { it.trim() }.takeIf { it.size >= 3 }?.let { collectionParts ->
                        val itemsBeforeTax = collectionParts[0].toInt()
                        val cashBeforeTax = collectionParts[1].toIntOrNull() ?: 0
                        val participants = collectionParts.drop(2).map { participantName ->
                            participants.getOrPut(participantName) { Participant(participantName) }
                        }
                        HaulInput(itemsBeforeTax, cashBeforeTax, participants)
                    }
                }
            }

            val hauls = haulInputs.map { haulInput ->
                val (itemsAfterTax, returnsFromItems, itemsTax) = calculateTaxAndReturns(haulInput.itemsBeforeTax)
                val (cashAfterTax, returnsFromCash, cashTax) = calculateTaxAndReturns(haulInput.cashBeforeTax)

                Haul(
                    itemsAfterTax,
                    cashAfterTax,
                    itemsTax,
                    cashTax,
                    returnsFromItems,
                    returnsFromCash,
                    haulInput.participants
                )
            }

            ContentInput(contentId, organizer, returnPointsPerHaul, hauls)
        }

        // Create Contents from ContentInputs
        // During this step, we should grant cash, items and returns to participants on a per-haul basis

        val currentParticipants = mutableListOf<Participant>()
        var currentItemsTotal = 0
        var currentCashTotal = 0

        contentLines.drop(1).forEach { line ->
            line.split(":").map { it.trim() }.takeIf { it.size == 2 }?.let { parts ->
                parts[1].split(",").map { it.trim() }.takeIf { it.size >= 3 }?.let { collectionParts ->
                    currentItemsTotal += collectionParts[0].toInt()
                    currentCashTotal += collectionParts[1].toIntOrNull() ?: 0
                    collectionParts.drop(2)
                        .forEach { participantName -> // TODO - Check if participantName does not include (50%)
                            participants.getOrPut(participantName) { Participant(participantName) }.also {
                                currentParticipants.add(it)
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


fun calculateTaxAndReturns(amount: Int): Triple<Int, Int, Int> {
    val afterTax = (amount * 0.8).toInt() / 1000 * 1000 // round down to thousands
    val returns = (amount * 0.1).toInt() / 1000 * 1000
    val tax = amount - afterTax - returns

    return Triple(afterTax, returns, tax)
}


fun main() {
}
