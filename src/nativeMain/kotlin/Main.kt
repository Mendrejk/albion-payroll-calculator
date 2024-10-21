import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.experimental.ExperimentalNativeApi


data class Participant(
    val name: String,
    var returnPoints: Double = 0.0,
    var itemsAfterTaxPerLocation: MutableMap<String, Int> = mutableMapOf(),
    var cashAfterTax: Int = 0,
    var returnsFromItems: Int = 0,
    var returnsFromCash: Int = 0
)

data class HaulParticipant(val participant: Participant, val hasFullShare: Boolean)

data class HaulInput(
    val itemsBeforeTax: Int,
    val cashBeforeTax: Int,
    val location: String,
    val hadOrganizer: Boolean,
    val participants: List<HaulParticipant>
)

data class ContentInput(
    val id: Int, val organizer: Participant?, val haulInputs: List<HaulInput>
)

data class RecruitmentInput(val recruiter: Participant, val points: Int)

typealias Participants = MutableMap<String, Participant>

data class Input(
    val contents: List<ContentInput>, val recruitments: List<RecruitmentInput>, val participants: Participants
)

data class Haul(
    val itemsAfterTax: Int,
    val cashAfterTax: Int,
    var itemsTax: Int,
    var cashTax: Int,
    val returnsFromItems: Int,
    val returnsFromCash: Int,
    val location: String,
    val hadOrganizer: Boolean,
    val participants: List<(HaulParticipant)>
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

data class Payroll(
    val itemsTaxTotal: Int, val cashTaxTotal: Int, val participants: Participants
)

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

fun calculateTaxAndReturns(amount: Int): Triple<Int, Int, Int> {
    val afterTax = (amount * 0.8).toInt() / 1000 * 1000 // round down to thousands
    val returns = (amount * 0.1).toInt() / 1000 * 1000
    val tax = amount - afterTax - returns

    return Triple(afterTax, returns, tax)
}


fun parseInputFile(): Input {
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

    val participants = mutableMapOf<String, Participant>()

    // TODO: add checks for invalid shape of the input file
    // if something is wrong, it should be logged
    val contentInputs = groupedContentsLines.map { contentLines ->
        val (contentId, organizer) = run {
            val contentHeader = contentLines.first().split(":").map { it.trim() }
            val contentId = contentHeader.getOrNull(0)?.toIntOrNull() ?: 0
            val organizer = contentHeader.getOrNull(1)?.takeIf { it.isNotEmpty() }?.let {
                participants.getOrPut(it) { Participant(it) } // TODO: organiser can also be counted as 50%!
            }
            Pair(contentId, organizer)
        }

        val haulsLines = contentLines.drop(1)

        val haulInputs = haulsLines.mapNotNull { haulLine ->
            haulLine.split(":").map { it.trim() }.takeIf { it.size == 2 }?.let { (_, haulData) ->
                haulData.split(",").map { it.trim() }.takeIf { it.size >= 5 }?.let { haulParts ->
                    val itemsBeforeTax = haulParts[0].toInt()
                    val cashBeforeTax = haulParts[1].toIntOrNull() ?: 0
                    val location = haulParts[2]
                    val hadOrganizer = haulParts[3] == "TAK"
                    val haulParticipants = haulParts.drop(4).map { participantString ->
                        val (participantName, hasFullShare) = parseParticipant(participantString)
                        val participant = participants.getOrPut(participantName) { Participant(participantName) }
                        HaulParticipant(participant, hasFullShare)
                    }
                    HaulInput(itemsBeforeTax, cashBeforeTax, location, hadOrganizer, haulParticipants)
                }
            }
        }

        ContentInput(contentId, organizer, haulInputs)
    }

    val recruitmentInputs = recruitmentsLines.mapNotNull { recruitmentLine ->
        recruitmentLine.split(":").map { it.trim() }.takeIf { it.size == 2 }
            ?.let { (recruiterName, recruitmentPoints) ->
                val recruiter = participants.getOrPut(recruiterName) { Participant(recruiterName) }
                RecruitmentInput(recruiter, recruitmentPoints.toInt())
            }
    }

    return Input(contentInputs, recruitmentInputs, participants)
}

@OptIn(ExperimentalNativeApi::class)
fun calculateItemsAndCash(contentInputs: List<ContentInput>): List<Content> {
    return contentInputs.map { contentInput ->
        val hauls = contentInput.haulInputs.map { haulInput ->
            val (itemsAfterTax, returnsFromItems, itemsTax) = calculateTaxAndReturns(haulInput.itemsBeforeTax)
            val (cashAfterTax, returnsFromCash, cashTax) = calculateTaxAndReturns(haulInput.cashBeforeTax)

            Haul(
                itemsAfterTax,
                cashAfterTax,
                itemsTax,
                cashTax,
                returnsFromItems,
                returnsFromCash,
                haulInput.location,
                haulInput.hadOrganizer,
                haulInput.participants
            )
        }

        // Create Contents from ContentInputs
        // During this step, we should grant cash, items and return points to participants on a per-haul basis
        val content = contentInput.run {
            val returnPointsPerHaul = organizer?.let {
                val numberOfHauls = hauls.size
                if (numberOfHauls > 0) (1.0 / numberOfHauls.toDouble()) else 0.0
            } ?: 0.0

            organizer?.let { organizer ->
                val organizerShareOfHauls = hauls.count { it.hadOrganizer } / hauls.count().toDouble()
                organizer.returnPoints += 1.0 * organizerShareOfHauls
            }

            hauls.forEach { haul ->
                val returnPointsPerParticipant = returnPointsPerHaul / haul.participants.size

                val haulSharePoints: Int =
                    if ((organizer != null) && haul.hadOrganizer) {
                        2
                    } else {
                        0
                    } + haul.participants.sumOf { (if (it.hasFullShare) 2 else 1).toInt() }
                val cashHaulShareUnit = (haul.cashAfterTax / haulSharePoints / 1000) * 1000
                val itemsHaulShareUnit = (haul.itemsAfterTax / haulSharePoints / 1000) * 1000

                val cashRemainder = haul.cashAfterTax - (cashHaulShareUnit * haulSharePoints)
                val itemsRemainder = haul.itemsAfterTax - (itemsHaulShareUnit * haulSharePoints)
                haul.cashTax += cashRemainder
                haul.itemsTax += itemsRemainder

                var itemsLeftToDistribute = haul.itemsAfterTax
//                println("Items left to distribute: $itemsLeftToDistribute")
                var cashLeftToDistribute = haul.cashAfterTax

                if (haul.hadOrganizer) {
                    organizer?.also {
                        val existingLocation = it.itemsAfterTaxPerLocation.contains(haul.location)
                        if (existingLocation) {
                            it.itemsAfterTaxPerLocation[haul.location] =
                                it.itemsAfterTaxPerLocation[haul.location]!! + itemsHaulShareUnit * 2
                        } else {
                            it.itemsAfterTaxPerLocation[haul.location] = itemsHaulShareUnit * 2
                        }

                        it.cashAfterTax += cashHaulShareUnit * 2
                        itemsLeftToDistribute -= itemsHaulShareUnit * 2
                        cashLeftToDistribute -= cashHaulShareUnit * 2
                    }
                }

                var sumOfDistributedItems: Int = 0

                haul.participants.forEach { haulParticipant ->
                    val participationShare = if (haulParticipant.hasFullShare) 2 else 1
                    val existingLocation = haulParticipant.participant.itemsAfterTaxPerLocation.contains(haul.location)
                    if (existingLocation) {
                        haulParticipant.participant.itemsAfterTaxPerLocation[haul.location] =
                            haulParticipant.participant.itemsAfterTaxPerLocation[haul.location]!! + itemsHaulShareUnit * participationShare
                    } else {
                        haulParticipant.participant.itemsAfterTaxPerLocation[haul.location] =
                            itemsHaulShareUnit * participationShare
                    }
                    haulParticipant.participant.cashAfterTax += cashHaulShareUnit * participationShare
                    haulParticipant.participant.returnPoints += returnPointsPerParticipant
                    itemsLeftToDistribute -= itemsHaulShareUnit * participationShare
                    cashLeftToDistribute -= cashHaulShareUnit * participationShare
                    sumOfDistributedItems += itemsHaulShareUnit * participationShare
                }

                println("Sum of distributed items: $sumOfDistributedItems")

                assert(itemsLeftToDistribute == itemsRemainder)
                assert(cashLeftToDistribute == cashRemainder)
            }

            Content(id,
                itemsTaxTotal = hauls.sumOf { it.itemsTax },
                cashTaxTotal = hauls.sumOf { it.cashTax },
                returnsFromItemsTotal = hauls.sumOf { it.returnsFromItems },
                returnsFromCashTotal = hauls.sumOf { it.returnsFromCash },
                organizer,
                participants = hauls.flatMap { it.participants }.map { it.participant }.distinctBy { it })
        }

        content
    }
}

fun distributeReturns(
    participants: Participants, contents: List<Content>, recruitmentReturnPoints: Double
): Pair<Int, Int> {
    // check if all return points assigned are equal to the sum of recruitment return points and (2 * number of contents with organizers)
    val expectedReturnPoints = recruitmentReturnPoints + (2 * contents.count { it.organizer != null })
    val returnPointsTotal = participants.values.sumOf { it.returnPoints }
    if (returnPointsTotal != expectedReturnPoints) {
        println("Return points do not add up! Expected: $expectedReturnPoints, actual: $returnPointsTotal") // TODO fix - organizer does not always have to get full points
    }

    // Sum the return from items and cash for all contents
    val allReturnsFromItems = contents.sumOf { it.returnsFromItemsTotal }
    val allReturnsFromCash = contents.sumOf { it.returnsFromCashTotal }
    // Calculate the value of one return point
    val valuePerReturnPointItems = allReturnsFromItems / returnPointsTotal
    val valuePerReturnPointCash = allReturnsFromCash / returnPointsTotal

    // Distribute the returns to each participant, rounding down to thousands.
    val (itemTaxFromReturnsRemainder, cashTaxFromReturnsRemainder) = participants.values.fold(0 to 0) { (itemRemainder, cashRemainder), participant ->
        val returnFromItemsToAdd = (participant.returnPoints * valuePerReturnPointItems).toInt() / 1000 * 1000
        val returnFromCashToAdd = (participant.returnPoints * valuePerReturnPointCash).toInt() / 1000 * 1000

        val itemDifference = (participant.returnPoints * valuePerReturnPointItems).toInt() - returnFromItemsToAdd
        val cashDifference = (participant.returnPoints * valuePerReturnPointCash).toInt() - returnFromCashToAdd

        participant.returnsFromItems += returnFromItemsToAdd
        participant.returnsFromCash += returnFromCashToAdd

        itemRemainder + itemDifference to cashRemainder + cashDifference
    }

    return itemTaxFromReturnsRemainder to cashTaxFromReturnsRemainder
}

fun calculatePayroll(): Payroll {
    val (contentInputs, recruitmentInputs, participants) = parseInputFile()

    val contents = calculateItemsAndCash(contentInputs)

    val recruitmentReturnPoints = recruitmentInputs.sumOf { recruitmentInput ->
        val recruitmentPoints = recruitmentInput.points.toDouble()
        recruitmentInput.recruiter.returnPoints += recruitmentPoints // give the recruiter his recruitment return points
        recruitmentPoints
    }

    val (itemTaxFromReturnsRemainder, cashTaxFromReturnsRemainder) = distributeReturns(
        participants, contents, recruitmentReturnPoints
    )
    val itemsTaxTotal = contents.sumOf { it.itemsTaxTotal } + itemTaxFromReturnsRemainder
    val cashTaxTotal = contents.sumOf { it.cashTaxTotal } + cashTaxFromReturnsRemainder

    return Payroll(itemsTaxTotal, cashTaxTotal, participants)
}

fun writePayrollOutput(payroll: Payroll) {
    val headers = mutableListOf("Nick", "Wypłata w gotówce", "Zwrot podatku", "Punkty Zwrotu Podatku")

    // Collect all unique locations
    val allLocations = payroll.participants.values.flatMap { it.itemsAfterTaxPerLocation.keys }.distinct()

    // Add headers for each location
    headers.addAll(allLocations.map { "Wypłata w przedmiotach $it" })

    // Calculate the maximum width for each column
    val columnWidths = headers.mapIndexed { index, header ->
        maxOf(header.length, payroll.participants.values.maxOf { participant ->
            when (index) {
                0 -> participant.name.length
                1 -> participant.cashAfterTax.toString().length
                2 -> (participant.returnsFromItems + participant.returnsFromCash).toString().length
                3 -> participant.returnPoints.toString().length
                else -> participant.itemsAfterTaxPerLocation[allLocations[index - 4]]?.toString()?.length ?: 0
            }
        })
    }

    val separator = columnWidths.joinToString(" | ") { "-".repeat(it) }
    val headerLine = headers.mapIndexed { index, header ->
        header.padEnd(columnWidths[index])
    }.joinToString(" | ")

    val rows = payroll.participants.values.map { participant ->
        val row = mutableListOf(
            participant.name.padEnd(columnWidths[0]),
            participant.cashAfterTax.toString().padEnd(columnWidths[1]),
            (participant.returnsFromItems + participant.returnsFromCash).toString().padEnd(columnWidths[2]),
            participant.returnPoints.toString().padEnd(columnWidths[3])
        )

        // Add values for each location
        row.addAll(allLocations.map { location ->
            participant.itemsAfterTaxPerLocation[location]?.toString()
                ?.padEnd(columnWidths[headers.indexOf("Wypłata w przedmiotach $location")])
                ?: "".padEnd(columnWidths[headers.indexOf("Wypłata w przedmiotach $location")])
        })

        row.joinToString(" | ")
    }

    val output = buildString {
        appendLine("Podatek w przedmiotach: ${payroll.itemsTaxTotal}")
        appendLine("Podatek w gotówce: ${payroll.cashTaxTotal}")
        appendLine("Suma wypłat w przedmiotach: ${payroll.participants.values.sumOf { it.itemsAfterTaxPerLocation.values.sum() }}")
        appendLine("Suma wypłat w gotówce: ${payroll.participants.values.sumOf { it.cashAfterTax }}")
        appendLine("Suma zwrotów w przedmiotach: ${payroll.participants.values.sumOf { it.returnsFromItems }}")
        appendLine("Suma zwrotów w gotówce: ${payroll.participants.values.sumOf { it.returnsFromCash }}")
        appendLine(headerLine)
        appendLine(separator)
        rows.forEach { appendLine(it) }
    }

    FileSystem.SYSTEM.write("wyjscie.txt".toPath()) {
        writeUtf8(output)
    }
}

fun main() {
    writePayrollOutput(calculatePayroll())
}
