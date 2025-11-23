import energy.runEnergyBalance
import okio.FileSystem
import okio.Path.Companion.toPath


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

    val (contentsLines, ctaLines, recruitmentsLines) = lines.fold(mutableListOf<MutableList<String>>()) { acc, line ->
        if (line.startsWith("KONTENTY:") || line.startsWith("CTA:") || line.startsWith("REKRUTACJA:")) {
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
                val lowercaseOrganizer = it.lowercase()
                participants.getOrPut(lowercaseOrganizer) { Participant(lowercaseOrganizer) } // TODO: organiser can also be counted as 50%!
            }
            Pair(contentId, organizer)
        }

        val haulsLines = contentLines.drop(1)

        val haulInputs = haulsLines.mapNotNull { haulLine ->
            haulLine.split(":").map { it.trim() }.takeIf { it.size == 2 }?.let { (_, haulData) ->
                haulData.split(",").map { it.trim() }.takeIf { it.size >= 7 }?.let { haulParts ->
                    // TODO: we should use thousands in the data class instead of multiplying by 1000 here
                    val itemsBeforeTax = haulParts[0].toInt() * 1000
                    val cashBeforeTax = haulParts[1].toIntOrNull()?.times(1000) ?: 0
                    val location = haulParts[2]
                    val tab = haulParts[3]
                    val hadOrganizer = haulParts[4].lowercase() == "tak"
                    val caller = haulParts[5].takeIf { it.isNotEmpty() }?.let {
                        val (callerName, hasFullShare) = parseParticipant(it.lowercase())
                        val caller = participants.getOrPut(callerName) { Participant(callerName) }
                        HaulParticipant(caller, hasFullShare)
                    }
                    val haulParticipants = HashSet(haulParts.drop(6).map { participantString ->
                        val (participantName, hasFullShare) = parseParticipant(participantString.lowercase())
                        val participant = participants.getOrPut(participantName) { Participant(participantName) }
                        HaulParticipant(participant, hasFullShare)
                    })
                    HaulInput(itemsBeforeTax, cashBeforeTax, location, tab, hadOrganizer, caller, haulParticipants)
                }
            }
        }

        ContentInput(contentId, organizer, haulInputs)
    }

    val ctaInputs = ctaLines.mapNotNull { ctaLine ->
        ctaLine.split(":").map { it.trim() }.takeIf { it.size == 2 }?.let { (ctaId, ctaData) ->
            ctaData.split("-").map { it.trim() }.takeIf { it.size == 2 }?.let { (callerName, ctaParticipantsData) ->
                val caller = participants.getOrPut(callerName.lowercase()) { Participant(callerName.lowercase()) }
                val ctaParticipants = HashSet(
                    ctaParticipantsData.split(",").map { it.trim().lowercase() }.map { ctaParticipantName ->
                        participants.getOrPut(ctaParticipantName) { Participant(ctaParticipantName) }
                    })
                CtaInput(ctaId.toIntOrNull() ?: 0 , caller, ctaParticipants)
            }
        }
    }

    val recruitmentInputs = recruitmentsLines.mapNotNull { recruitmentLine ->
        recruitmentLine.split(":").map { it.trim() }.takeIf { it.size == 2 }
            ?.let { (recruiterName, recruitmentPoints) ->
                val lowercaseRecruiterName = recruiterName.lowercase()
                val recruiter = participants.getOrPut(lowercaseRecruiterName) { Participant(lowercaseRecruiterName) }
                RecruitmentInput(recruiter, recruitmentPoints.toDouble())
            }
    }

    return Input(contentInputs, ctaInputs, recruitmentInputs, participants)
}

// File I/O and parsing - native-specific

fun writePayrollOutput(payroll: Payroll) {
    val headers = mutableListOf("Nick", "Wypłata w gotówce", "Zwrot podatku", "Punkty Zwrotu Podatku")

    // Collect all unique location-tab pairs
    val allLocationTabs = payroll.participants.values.flatMap { participant ->
        participant.itemsAfterTaxPerTabPerLocation.flatMap { (location, tabs) ->
            tabs.keys.map { tab -> "$location - $tab" }
        }
    }.distinct().sorted()

    // Add headers for each location-tab pair
    headers.addAll(allLocationTabs.map { "LOOT $it" })

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
                    formatNumberWithSpacesAndK(
                        participant.itemsAfterTaxPerTabPerLocation[location]?.get(tab) ?: 0
                    ).length
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
                .padEnd(columnWidths[headers.indexOf("LOOT $locationTab")])
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

    FileSystem.SYSTEM.write("wyjscie_${getCurrentFormattedDate()}.txt".toPath()) {
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
            val locationName = when (location) {
                "W KASIE" -> "W KASIE / CASH"
                "ZWROT PODATKU (CZAS NA ODEBRANIE DO NASTĘPNEGO ROZLICZENIA)" -> location
                else -> "LOOT $location"
            }
            appendLine("## ${locationName.uppercase()}")
            val entries = locationSums[location]?.entries?.sortedByDescending { it.value } ?: emptyList()
            entries.forEachIndexed {index, (participant, amount) ->
                if (locationName.startsWith("LOOT "))
                {
                    appendLine("${index + 1}: @$participant ${formatNumberWithSpaces(amount / 1000)}k")
                } else {
                    if (amount > 0) {
                        appendLine("@$participant ${formatNumberWithSpaces(amount / 1000)}k")
                    }
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
            appendLine("## LOOT ${location.uppercase()} - ${tab.uppercase()}")
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

    FileSystem.SYSTEM.write("wyjscie_discord_${getCurrentFormattedDate()}.md".toPath()) {
        writeUtf8(markdownContent)
    }
}

fun runPayroll() {
    val input = parseInputFile()
    val payroll = calculatePayroll(input)
    writePayrollOutput(payroll)
    writePayrollToMarkdown(payroll)
    printSimilarNames(payroll.participants)
}

fun main() {
    println("Enter 'p' for payroll or 'e' for energy balance or 'a' for all:")
    val userChoice = readlnOrNull()?.trim()?.lowercase()
    when (userChoice) {
        "p" -> runPayroll()
        "e" -> runEnergyBalance()
        "a" -> {
            runPayroll()
            runEnergyBalance()
        }
        else -> println("Invalid option.")
    }
}