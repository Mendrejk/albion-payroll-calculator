import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.experimental.ExperimentalNativeApi


data class Participant(
    val name: String,
    var returnPoints: Double = 0.0,
    var itemsAfterTax: Int = 0,
    var cashAfterTax: Int = 0,
    var returnsTotal: Int = 0
)

data class HaulParticipant(val participant: Participant, val hasFullShare: Boolean)

data class HaulInput(val itemsBeforeTax: Int, val cashBeforeTax: Int, val participants: List<HaulParticipant>)
data class Haul(
    val itemsAfterTax: Int,
    val cashAfterTax: Int,
    var itemsTax: Int,
    var cashTax: Int,
    val returnsFromItems: Int,
    val returnsFromCash: Int,
    val participants: List<(HaulParticipant)>
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


fun parseParticipant(participantString: String): Pair<String, Boolean> {
    val trimmedName = participantString.trim()
    val hasReducedShare = trimmedName.endsWith("(50%)")
    val name = if (hasReducedShare) {
        trimmedName.removeSuffix("(50%)").trim()
    } else {
        trimmedName
    }
    return Pair(name, hasReducedShare)
}

fun calculateTaxAndReturns(amount: Int): Triple<Int, Int, Int> {
    val afterTax = (amount * 0.8).toInt() / 1000 * 1000 // round down to thousands
    val returns = (amount * 0.1).toInt() / 1000 * 1000
    val tax = amount - afterTax - returns

    return Triple(afterTax, returns, tax)
}


@OptIn(ExperimentalNativeApi::class)
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
                val organizer =
                    contentHeader.getOrNull(1)?.takeIf { it.isNotEmpty() }?.let { // TODO: can the organiser be 50%?
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
                    haulData.split(",").map { it.trim() }.takeIf { it.size >= 3 }?.let { haulParts ->
                        val itemsBeforeTax = haulParts[0].toInt()
                        val cashBeforeTax = haulParts[1].toIntOrNull() ?: 0
                        val participants = haulParts.drop(2).map { participantString ->
                            val (participantName, hasFullShare) = parseParticipant(participantString)
                            val participant = participants.getOrPut(participantName) { Participant(participantName) }
                            HaulParticipant(participant, hasFullShare)
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
        // During this step, we should grant cash, items and return points to participants on a per-haul basis
        val content = contentInput.run {
            hauls.forEach { haul ->
                val returnPointsPerParticipant = returnPointsPerHaul / haul.participants.size
                val haulSharePoints: Int =
                    if (organizer != null) 2 else 0 + haul.participants.sumOf { (if (it.hasFullShare) 2 else 1).toInt() }

                val cashUnit = (haul.cashAfterTax / haulSharePoints / 1000) * 1000
                val itemsUnit = (haul.itemsAfterTax / haulSharePoints / 1000) * 1000

                val cashRemainder = haul.cashAfterTax - (cashUnit * haulSharePoints)
                val itemsRemainder = haul.itemsAfterTax - (itemsUnit * haulSharePoints)
                haul.cashTax += cashRemainder
                haul.itemsTax += itemsRemainder

                var itemsLeftToDistribute = haul.itemsAfterTax
                var cashLeftToDistribute = haul.cashAfterTax

                organizer?.also {
                    it.itemsAfterTax += itemsUnit * 2
                    it.cashAfterTax += cashUnit * 2
                    itemsLeftToDistribute -= itemsUnit * 2
                    cashLeftToDistribute -= cashUnit * 2
                }

                haul.participants.forEach { haulParticipant ->
                    val participationShare = if (haulParticipant.hasFullShare) 2 else 1
                    haulParticipant.participant.itemsAfterTax += itemsUnit * participationShare
                    haulParticipant.participant.cashAfterTax += cashUnit * participationShare
                    haulParticipant.participant.returnPoints += returnPointsPerParticipant
                    itemsLeftToDistribute -= itemsUnit * participationShare
                    cashLeftToDistribute -= cashUnit * participationShare
                }

                assert(itemsLeftToDistribute == 0)
                assert(cashLeftToDistribute == 0)
            }

            Content(
                id,
                itemsTaxTotal = hauls.sumOf { it.itemsTax },
                cashTaxTotal = hauls.sumOf { it.cashTax },
                returnsFromItemsTotal = hauls.sumOf { it.returnsFromItems },
                returnsFromCashTotal = hauls.sumOf { it.returnsFromCash },
                organizer,
                participants = hauls.flatMap { it.participants }.map { it.participant }.distinctBy { it }
            )
        }

    TODO()
}

fun calculate_payroll() {
    // 1. Load the input file
}

fun main() {
}
