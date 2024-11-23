import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.experimental.ExperimentalNativeApi


data class Participant(
    val name: String,
    var returnPoints: Double = 0.0,
    var itemsAfterTaxPerTabPerLocation: MutableMap<String, MutableMap<String, Int>> = mutableMapOf(),
    var cashAfterTax: Int = 0,
    var returnsFromItems: Int = 0,
    var returnsFromCash: Int = 0
)

data class HaulParticipant(val participant: Participant, val hasFullShare: Boolean)

data class HaulInput(
    val itemsBeforeTax: Int,
    val cashBeforeTax: Int,
    val location: String,
    val tab: String,
    val hadOrganizer: Boolean,
    val caller: HaulParticipant?,
    val participants: List<HaulParticipant>
)

data class ContentInput(
    val id: Int, val organizer: Participant?, val haulInputs: List<HaulInput>
)

data class RecruitmentInput(val recruiter: Participant, val points: Double)

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
    val tab: String,
    val hadOrganizer: Boolean,
    val caller: HaulParticipant?,
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
                haulData.split(",").map { it.trim() }.takeIf { it.size >= 7 }?.let { haulParts ->
                    val itemsBeforeTax = haulParts[0].toInt()
                    val cashBeforeTax = haulParts[1].toIntOrNull() ?: 0
                    val location = haulParts[2]
                    val tab = haulParts[3]
                    val hadOrganizer = haulParts[4] == "TAK"
                    val caller = haulParts[5].takeIf { it.isNotEmpty() }?.let {
                        val (callerName, hasFullShare) = parseParticipant(it)
                        val caller = participants.getOrPut(callerName) { Participant(callerName) }
                        HaulParticipant(caller, hasFullShare)
                    }
                    val haulParticipants = haulParts.drop(6).map { participantString ->
                        val (participantName, hasFullShare) = parseParticipant(participantString)
                        val participant = participants.getOrPut(participantName) { Participant(participantName) }
                        HaulParticipant(participant, hasFullShare)
                    }
                    HaulInput(itemsBeforeTax, cashBeforeTax, location, tab, hadOrganizer, caller, haulParticipants)
                }
            }
        }

        ContentInput(contentId, organizer, haulInputs)
    }

    val recruitmentInputs = recruitmentsLines.mapNotNull { recruitmentLine ->
        recruitmentLine.split(":").map { it.trim() }.takeIf { it.size == 2 }
            ?.let { (recruiterName, recruitmentPoints) ->
                val recruiter = participants.getOrPut(recruiterName) { Participant(recruiterName) }
                RecruitmentInput(recruiter, recruitmentPoints.toDouble())
            }
    }

    return Input(contentInputs, recruitmentInputs, participants)
}

fun determineReturnMultiplier(howManyParticipants: Int): Double {
    if (howManyParticipants == 1) return 1.0
    return ((howManyParticipants - 1) / 5 + 1).toDouble() // 1 for 1-5, 2 for 6-10, 3 for 11-15, etc.
}

const val PARTICIPANT_RETURN_BASE = 0.05
const val ORGANISER_CALLER_RETURN_BASE = 0.25

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
                haulInput.tab,
                haulInput.hadOrganizer,
                haulInput.caller,
                haulInput.participants
            )
        }

        // Create Contents from ContentInputs
        // During this step, we should grant cash, items and return points to participants on a per-haul basis
        val content = contentInput.run {
            val allParticipantsCount = (listOfNotNull(organizer) + hauls.flatMap { haul ->
                listOfNotNull(haul.caller?.participant) + haul.participants.map { it.participant }
            }).distinctBy { it.name }.count()
            val returnMultiplier = determineReturnMultiplier(allParticipantsCount)
            val participantReturnPerHaul = PARTICIPANT_RETURN_BASE * returnMultiplier / hauls.size.toDouble()
            val organizerCallerReturnPerHaul = ORGANISER_CALLER_RETURN_BASE * returnMultiplier / hauls.size.toDouble()

            hauls.forEach { haul ->
                val isCallerSameAsOrganizer = haul.caller?.participant?.name == organizer?.name

                val haulSharePoints =
                    (if (organizer != null && haul.hadOrganizer) 2 else 0) +
                            (haul.caller?.let { if (isCallerSameAsOrganizer) 0 else if (it.hasFullShare) 2 else 1 }
                                ?: 0) +
                            haul.participants.sumOf { (if (it.hasFullShare) 2 else 1).toInt() }
                val cashHaulShareUnit = (haul.cashAfterTax / haulSharePoints / 1000) * 1000
                val itemsHaulShareUnit = (haul.itemsAfterTax / haulSharePoints / 1000) * 1000

                val cashRemainder = haul.cashAfterTax - (cashHaulShareUnit * haulSharePoints)
                val itemsRemainder = haul.itemsAfterTax - (itemsHaulShareUnit * haulSharePoints)
                haul.cashTax += cashRemainder
                haul.itemsTax += itemsRemainder

                var itemsLeftToDistribute = haul.itemsAfterTax
                var cashLeftToDistribute = haul.cashAfterTax

                if (haul.hadOrganizer) {
                    organizer?.also {
                        val location = it.itemsAfterTaxPerTabPerLocation.getOrPut(haul.location) { mutableMapOf() }
                        location[haul.tab] = location.getOrPut(haul.tab) { 0 } + itemsHaulShareUnit * 2

                        it.cashAfterTax += cashHaulShareUnit * 2
                        itemsLeftToDistribute -= itemsHaulShareUnit * 2
                        cashLeftToDistribute -= cashHaulShareUnit * 2
                    }
                }

                if (!isCallerSameAsOrganizer) {
                    haul.caller?.also {
                        val participationShare = if (it.hasFullShare) 2 else 1
                        val location =
                            it.participant.itemsAfterTaxPerTabPerLocation.getOrPut(haul.location) { mutableMapOf() }
                        location[haul.tab] = location.getOrPut(haul.tab) { 0 } + itemsHaulShareUnit * participationShare

                        it.participant.cashAfterTax += cashHaulShareUnit * participationShare
                        itemsLeftToDistribute -= itemsHaulShareUnit * participationShare
                        cashLeftToDistribute -= cashHaulShareUnit * participationShare
                    }
                }

                if (haul.hadOrganizer) {
                    organizer?.let {
                        it.returnPoints += organizerCallerReturnPerHaul
                    }
                }
                haul.caller?.let {
                    it.participant.returnPoints += organizerCallerReturnPerHaul
                }

                haul.participants.forEach { haulParticipant ->
                    val participationShare = if (haulParticipant.hasFullShare) 2 else 1

                    val location =
                        haulParticipant.participant.itemsAfterTaxPerTabPerLocation.getOrPut(haul.location) { mutableMapOf() }
                    location[haul.tab] = location.getOrPut(haul.tab) { 0 } + itemsHaulShareUnit * participationShare

                    haulParticipant.participant.cashAfterTax += cashHaulShareUnit * participationShare
                    haulParticipant.participant.returnPoints += participantReturnPerHaul
                    itemsLeftToDistribute -= itemsHaulShareUnit * participationShare
                    cashLeftToDistribute -= cashHaulShareUnit * participationShare
                }

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
    val returnPointsTotal = participants.values.sumOf { it.returnPoints }

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

fun formatNumberWithSpaces(number: Int): String {
    return number.toString().reversed().chunked(3).joinToString(" ").reversed()
}

fun formatNumberWithSpacesAndK(number: Int): String {
    return "${formatNumberWithSpaces(number / 1000)}k"
}

fun writePayrollOutput(payroll: Payroll) {
    val headers = mutableListOf("Nick", "Wypłata w gotówce", "Zwrot podatku", "Punkty Zwrotu Podatku")

    // Collect all unique location-tab pairs
    val allLocationTabs = payroll.participants.values.flatMap { participant ->
        participant.itemsAfterTaxPerTabPerLocation.flatMap { (location, tabs) ->
            tabs.keys.map { tab -> "$location - $tab" }
        }
    }.distinct().sorted()

    // Add headers for each location-tab pair
    headers.addAll(allLocationTabs.map { "Wypłata w przedmiotach $it" })

    // Calculate the maximum width for each column
    val columnWidths = headers.mapIndexed { index, header ->
        maxOf(header.length, payroll.participants.values.maxOf { participant ->
            when (index) {
                0 -> participant.name.length
                1 -> formatNumberWithSpacesAndK(participant.cashAfterTax).length
                2 -> formatNumberWithSpacesAndK(participant.returnsFromItems + participant.returnsFromCash).length
                3 -> participant.returnPoints.toString().length
                else -> {
                    val (location, tab) = allLocationTabs[index - 4].split(" - ")
                    formatNumberWithSpacesAndK(participant.itemsAfterTaxPerTabPerLocation[location]?.get(tab) ?: 0).length
                }
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
            formatNumberWithSpacesAndK(participant.cashAfterTax).padEnd(columnWidths[1]),
            formatNumberWithSpacesAndK(participant.returnsFromItems + participant.returnsFromCash).padEnd(columnWidths[2]),
            participant.returnPoints.toString().padEnd(columnWidths[3])
        )

        // Add values for each location-tab pair
        row.addAll(allLocationTabs.map { locationTab ->
            val (location, tab) = locationTab.split(" - ").map { it.trim() }
            formatNumberWithSpacesAndK(participant.itemsAfterTaxPerTabPerLocation[location]?.get(tab) ?: 0)
                .padEnd(columnWidths[headers.indexOf("Wypłata w przedmiotach $locationTab")])
        })

        row.joinToString(" | ")
    }

    val output = buildString {
        appendLine("Podatek w przedmiotach: ${formatNumberWithSpacesAndK(payroll.itemsTaxTotal)}")
        appendLine("Podatek w gotówce: ${formatNumberWithSpacesAndK(payroll.cashTaxTotal)}")
        appendLine("Suma wypłat w przedmiotach: ${formatNumberWithSpacesAndK(payroll.participants.values.sumOf { it.itemsAfterTaxPerTabPerLocation.values.sumOf { tabs -> tabs.values.sum() } })}")
        appendLine("Suma wypłat w gotówce: ${formatNumberWithSpacesAndK(payroll.participants.values.sumOf { it.cashAfterTax })}")
        appendLine("Suma zwrotów w przedmiotach: ${formatNumberWithSpacesAndK(payroll.participants.values.sumOf { it.returnsFromItems })}")
        appendLine("Suma zwrotów w gotówce: ${formatNumberWithSpacesAndK(payroll.participants.values.sumOf { it.returnsFromCash })}")
        appendLine(headerLine)
        appendLine(separator)
        rows.forEach { appendLine(it) }
    }

    FileSystem.SYSTEM.write("wyjscie.txt".toPath()) {
        writeUtf8(output)
    }
}

fun sumCashAndItemsByLocation(participants: Participants): Map<String, Map<String, Int>> {
    val locationSums = mutableMapOf<String, MutableMap<String, Int>>()

    participants.values.forEach { participant ->
        participant.itemsAfterTaxPerTabPerLocation.forEach { (location, tabs) ->
            val locationSum = locationSums.getOrPut(location) { mutableMapOf() }
            tabs.forEach { (tab, items) ->
                locationSum[participant.name] = (locationSum[participant.name] ?: 0) + items
            }
        }
        locationSums.getOrPut("W KASIE") { mutableMapOf() }[participant.name] =
            (locationSums.getOrPut("W KASIE") { mutableMapOf() }[participant.name] ?: 0) + participant.cashAfterTax
        locationSums.getOrPut("ZWROT PODATKU (CZAS NA ODEBRANIE DO NASTĘPNEGO ROZLICZENIA)") { mutableMapOf() }[participant.name] =
            (locationSums.getOrPut("ZWROT PODATKU (CZAS NA ODEBRANIE DO NASTĘPNEGO ROZLICZENIA)") { mutableMapOf() }[participant.name]
                ?: 0) + participant.returnsFromItems + participant.returnsFromCash
    }

    return locationSums
}

fun writePayrollToMarkdown(payroll: Payroll) {
    val locationSums = sumCashAndItemsByLocation(payroll.participants)

    val sortedLocations = locationSums.keys.sortedWith(compareByDescending<String> { location ->
        when (location) {
            "W KASIE" -> Int.MAX_VALUE
            "ZWROT PODATKU (CZAS NA ODEBRANIE DO NASTĘPNEGO ROZLICZENIA)" -> Int.MAX_VALUE - 1
            else -> locationSums[location]?.values?.sum() ?: 0
        }
    })

    val markdownContent = buildString {
        sortedLocations.forEach { location ->
            appendLine("## ${location.uppercase()}")
            locationSums[location]?.entries?.sortedByDescending { it.value }?.forEach { (participant, amount) ->
                if (amount > 0) {
                    appendLine("@$participant ${formatNumberWithSpaces(amount / 1000)}k")
                }
            }
            appendLine()
        }

        payroll.participants.values.flatMap { participant ->
            participant.itemsAfterTaxPerTabPerLocation.flatMap { (location, tabs) ->
                tabs.keys.map { tab -> Triple(location, tab, participant) }
            }
        }.groupBy { it.first to it.second }.forEach { (locationTab, participants) ->
            val (location, tab) = locationTab
            appendLine("## ${location.uppercase()} - ${tab.uppercase()}")
            participants.groupBy { it.third.name }.mapValues { (_, groupedParticipants) ->
                groupedParticipants.sumOf { it.third.itemsAfterTaxPerTabPerLocation[location]?.get(tab) ?: 0 }
            }.entries.sortedByDescending { it.value }.forEach { (participant, amount) ->
                if (amount > 0) {
                    appendLine("@$participant ${formatNumberWithSpaces(amount / 1000)}k")
                }
            }
            appendLine()
        }
    }

    FileSystem.SYSTEM.write("wyjscie_discord.md".toPath()) {
        writeUtf8(markdownContent)
    }
}

fun main() {
    val payroll = calculatePayroll()
    writePayrollOutput(payroll)
    writePayrollToMarkdown(payroll)
}
